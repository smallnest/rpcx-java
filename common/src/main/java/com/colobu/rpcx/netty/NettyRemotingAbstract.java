package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.common.RemotingHelper;
import com.colobu.rpcx.common.RemotingUtil;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.protocol.RemotingSysResponseCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author goodjava@qq.com
 */
public class NettyRemotingAbstract {

    private static final Logger plog = LoggerFactory.getLogger(NettyRemotingAbstract.class);

    protected final ConcurrentHashMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);

    protected final HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable = new HashMap<>(64);

    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    protected final NettyEventExecuter nettyEventExecuter = new NettyEventExecuter();

    protected final ChannelEventListener channelEventListener = new ClientChannelEventListener();

    protected final ConcurrentHashMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    protected final Lock lockChannelTables = new ReentrantLock();

    protected static final long LockTimeoutMillis = 3000;

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecuter.putNettyEvent(event);
    }


    public void scanResponseTable() {
        final List<ResponseFuture> rfList = new LinkedList<>();
        //是协议对应号
        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();
            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                rep.release();
                it.remove();
                rfList.add(rep);
                plog.warn("remove timeout request, " + rep);
            }
        }

        for (ResponseFuture rf : rfList) {
            try {
                rf.executeInvokeCallback();
            } catch (Throwable e) {
                plog.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    /**
     * 关闭channel
     *
     * @param channel
     */
    public void closeChannel(final Channel channel) {
        if (null == channel) {
            return;
        }

        try {
            if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    ChannelWrapper prevCW = null;
                    String addrRemote = null;
                    for (Map.Entry<String, ChannelWrapper> entry : channelTables.entrySet()) {
                        String key = entry.getKey();
                        ChannelWrapper prev = entry.getValue();
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
                                prevCW = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }

                    if (null == prevCW) {
                        plog.info("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        plog.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                        RemotingUtil.closeChannel(channel);
                    }
                } catch (Exception e) {
                    plog.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                plog.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            plog.error("closeChannel exception", e);
        }
    }


    public void processMessageReceived(ChannelHandlerContext ctx, RemotingCommand msg) {
        final RemotingCommand cmd = msg;
        if (cmd != null) {
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequestCommand(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    public void processRequestCommand(final ChannelHandlerContext ctx, final RemotingCommand cmd) {
        final Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<NettyRequestProcessor, ExecutorService> pair = null == matched ? this.defaultRequestProcessor : matched;
        final int opaque = cmd.getOpaque();

        if (pair != null) {
            Runnable run = () -> {
                try {

                    final RemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
                    //是否是oneway是根据来的request计算的
                    if (!cmd.isOnewayRPC()) {
                        if (response != null) {
                            response.setOpaque(opaque);
                            response.markResponseType();
                            try {
                                ctx.writeAndFlush(response);
                            } catch (Throwable e) {
                                plog.error("process request over, but response failed", e);
                                plog.error(cmd.toString());
                                plog.error(response.toString());
                            }
                        } else {

                        }
                    }
                } catch (Throwable e) {
                    plog.error("process request exception", e);
                    plog.error(cmd.toString());

                    if (!cmd.isOnewayRPC()) {
                        final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_ERROR,
                                RemotingHelper.exceptionSimpleDesc(e));
                        response.setOpaque(opaque);
                        ctx.writeAndFlush(response);
                    }
                }
            };

            if (pair.getObject1().rejectRequest()) {
                final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY,
                        "[REJECTREQUEST]system busy, start flow control for a while");
                response.setOpaque(opaque);
                ctx.writeAndFlush(response);
                return;
            }

            try {
                final RequestTask requestTask = new RequestTask(run, ctx.channel(), cmd);
                pair.getObject2().submit(requestTask);
            } catch (RejectedExecutionException e) {
                if ((System.currentTimeMillis() % 10000) == 0) {
                    plog.warn(RemotingHelper.parseChannelRemoteAddr(ctx.channel())
                            + ", too many requests and system thread pool busy, RejectedExecutionException "
                            + pair.getObject2().toString()
                            + " request code: " + cmd.getCode());
                }

                if (!cmd.isOnewayRPC()) {
                    final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY,
                            "[OVERLOAD]system busy, start flow control for a while");
                    response.setOpaque(opaque);
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            String error = " request type " + cmd.getCode() + " not supported";
            final RemotingCommand response =
                    RemotingCommand.createResponseCommand(RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
            response.setOpaque(opaque);
            ctx.writeAndFlush(response);
            plog.error(RemotingHelper.parseChannelRemoteAddr(ctx.channel()) + error);
        }
    }


    /**
     * 处理返回结果
     *
     * @param ctx
     * @param cmd
     */
    public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {
        //获取request带过去的唯一码
        final int opaque = cmd.getOpaque();
        //查询responseFuture
        final ResponseFuture responseFuture = this.responseTable.get(opaque);
        if (responseFuture != null) {
            //这里并不解除阻塞
            responseFuture.setResponseCommand(cmd);
            responseFuture.release();
            this.responseTable.remove(opaque);
            if (responseFuture.getInvokeCallback() != null) {
                boolean runInThisThread = false;
                ExecutorService executor = this.getCallbackExecutor();
                if (executor != null) {
                    try {
                        executor.submit(() -> {
                            try {
                                responseFuture.executeInvokeCallback();
                            } catch (Throwable e) {
                                plog.warn("execute callback in executor exception, and callback throw", e);
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        plog.warn("execute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Throwable e) {
                        plog.warn("executeInvokeCallback Exception", e);
                    }
                }
            } else {
                //解除阻塞
                responseFuture.putResponse(cmd);
            }
        } else {
            plog.warn("receive response, but not matched any request, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
            plog.warn(cmd.toString());
        }
    }

    private ExecutorService callbackExecutor = Executors.newFixedThreadPool(5);

    private ExecutorService getCallbackExecutor() {
        return callbackExecutor;
    }

    public boolean hasEventListener() {
        return this.channelEventListener != null;
    }

    public void registerProcessor(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<NettyRequestProcessor, ExecutorService>(processor, executor);
        this.processorTable.put(requestCode, pair);
    }

}
