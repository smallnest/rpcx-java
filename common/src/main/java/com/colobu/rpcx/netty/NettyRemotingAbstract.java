package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.common.RemotingHelper;
import com.colobu.rpcx.common.RemotingUtil;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.protocol.RemotingSysResponseCode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.Epoll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author goodjava@qq.com
 */
public class NettyRemotingAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingAbstract.class);

    protected final ConcurrentHashMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);

    protected final HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable = new HashMap<>(64);

    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    protected final NettyEventExecuter nettyEventExecuter = new NettyEventExecuter();

    protected final ChannelEventListener channelEventListener = new ClientChannelEventListener();

    protected final ConcurrentHashMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    protected final Lock lockChannelTables = new ReentrantLock();

    protected static final long LockTimeoutMillis = 3000;

    private AtomicInteger processingRequest = new AtomicInteger();

    private volatile boolean stop = false;

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecuter.putNettyEvent(event);
    }


    public void scanProcessingRequest() {
        logger.info("processing request num:{}", processingRequest.get());
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
                logger.warn("remove timeout request:{} ", rep);
            }
        }

        for (ResponseFuture rf : rfList) {
            try {
                rf.executeInvokeCallback();
            } catch (Throwable e) {
                logger.warn("scanResponseTable, operationComplete Exception", e);
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
                        logger.info("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                        RemotingUtil.closeChannel(channel);
                    }
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
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
        final int opaque = cmd.getOpaque();
        if (this.stop) {
            if (!cmd.isOnewayRPC()) {
                final RemotingCommand response = RemotingCommand.createResponseCommand();
                Message message = new Message();
                message.setMessageType(MessageType.Response);
                message.setSeq(opaque);
                message.metadata.put(Constants.RPCX_ERROR_CODE, "-9");
                message.metadata.put(Constants.RPCX_ERROR_MESSAGE, "server begin shutdown");
                response.setMessage(message);
                response.setOpaque(opaque);
                ctx.writeAndFlush(response);
            }
            return;
        }


        final Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<NettyRequestProcessor, ExecutorService> pair = null == matched ? this.defaultRequestProcessor : matched;

        if (pair != null) {
            Runnable run = () -> {
                try {
                    processingRequest.incrementAndGet();
                    final RemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
                    //是否是oneway是根据来的request计算的
                    if (!cmd.isOnewayRPC()) {
                        if (response != null) {
                            response.setOpaque(opaque);
                            response.markResponseType();
                            try {
                                ctx.writeAndFlush(response);
                            } catch (Throwable e) {
                                logger.error("process request over, but response failed", e);
                                logger.error(cmd.toString());
                                logger.error(response.toString());
                            }
                        } else {

                        }
                    }
                } catch (Throwable e) {
                    logger.error("process request exception", e);
                    logger.error(cmd.toString());
                    if (!cmd.isOnewayRPC()) {
                        final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_ERROR,
                                RemotingHelper.exceptionSimpleDesc(e));
                        response.setOpaque(opaque);
                        ctx.writeAndFlush(response);
                    }
                } finally {
                    processingRequest.decrementAndGet();
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
                    logger.warn(RemotingHelper.parseChannelRemoteAddr(ctx.channel())
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
            logger.error(RemotingHelper.parseChannelRemoteAddr(ctx.channel()) + error);
        }
    }

    protected boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }


    /**
     * 处理返回结果
     *
     * @param ctx
     * @param cmd
     */
    public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {
        cmd.decode();
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
                                logger.warn("execute callback in executor exception, and callback throw", e);
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        logger.warn("execute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Throwable e) {
                        logger.warn("executeInvokeCallback Exception", e);
                    }
                }
            } else {
                //解除阻塞
                responseFuture.putResponse(cmd);
            }
        } else {
            logger.warn("receive response, but not matched any request, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
            logger.warn(cmd.toString());
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
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executor);
        this.processorTable.put(requestCode, pair);
    }

    public void serverShutdown() {
        logger.info("server shutdown begin");
        stop = true;
        int i = 0;
        while (true) {
            if (i++ >= 30) {
                break;
            }
            int num = this.processingRequest.get();
            logger.info("shutdown processing request num:{}", num);
            if (num == 0) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("server shutdown finish");
    }

}
