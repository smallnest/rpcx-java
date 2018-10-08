package com.colobu.rpcx.client;

import com.colobu.rpcx.common.*;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.config.NettyClientConfig;
import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.exception.RemotingSendRequestException;
import com.colobu.rpcx.exception.RemotingTimeoutException;
import com.colobu.rpcx.exception.RemotingTooMuchRequestException;
import com.colobu.rpcx.netty.*;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.rpc.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author goodjava@qq.com
 */
public class NettyClient extends NettyRemotingAbstract implements IClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private NettyClientConfig nettyClientConfig = new NettyClientConfig();

    private final EventLoopGroup eventLoopGroupWorker;

    private final long CallTimeOut = TimeUnit.SECONDS.toMillis(3);

    protected final NettyEventExecuter nettyEventExecuter = new NettyEventExecuter();

    private Bootstrap bootstrap;

    private final IServiceDiscovery serviceDiscovery;

    protected final Semaphore semaphoreAsync;

    protected final Semaphore semaphoreOneway;


    public NettyClient(IServiceDiscovery serviceDiscovery) {
        this.semaphoreOneway = new Semaphore(1000, true);
        this.semaphoreAsync = new Semaphore(1000, true);
        this.serviceDiscovery = serviceDiscovery;
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new NamedThreadFactory("NettyClientSelector_"));
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(nettyClientConfig.getClientWorkerThreads(), new NamedThreadFactory("NettyClientWorkerThread_"));

        this.bootstrap = createBootstrap();

        startScanResponseTableSchedule();
        runEventListener();
    }

    private void runEventListener() {
        if (this.channelEventListener != null) {
            new Thread(this.nettyEventExecuter).start();
        }
    }

    private Bootstrap createBootstrap() {
        return new Bootstrap().group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyClientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, nettyClientConfig.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, nettyClientConfig.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new NettyEncoder(),
                                new NettyDecoder(),
                                //*空闲状态的handler
                                new IdleStateHandler(0, 0, nettyClientConfig.getClientChannelMaxIdleTimeSeconds()),
                                //管理连接的
                                new NettyConnetManageHandler(NettyClient.this),
                                //处理具体业务逻辑的handler
                                new NettyClientHandler(NettyClient.this));
                    }
                });
    }

    public void startScanResponseTableSchedule() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("ClientHouseKeepingService"));
        executor.scheduleAtFixedRate(() -> {
            try {
                NettyClient.this.scanResponseTable();//* 查询response表
            } catch (Exception e) {
                logger.error("scanResponseTable exception", e);
            }
        }, 3000, 3000, TimeUnit.MILLISECONDS);
    }


    @Override
    public Message call(String addr, Message req, long timeOut) throws Exception {
        final RemotingCommand request = RemotingCommand.createRequestCommand(req);
        long timeoutMillis = timeOut;
        //获取或者创建channel
        final Channel channel = this.getAndCreateChannel(addr);
        //同步执行
        RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);
        return response.getMessage();
    }


    @Override
    public Message call(Message req, long timeoutMillis, String sendType) throws Exception {
        final RemotingCommand request = RemotingCommand.createRequestCommand(req);

        Channel channel = null;
        String serviceAddr = RpcContext.getContext().getServiceAddr();
        logger.info("---------||||---->serviceAddr:{}", serviceAddr);
        if (StringUtils.isEmpty(serviceAddr)) {
            throw new RpcException("service addr is null  call method:" + req.getServiceMethod());
        } else {
            channel = this.getAndCreateChannel(serviceAddr);
        }
        setHostAndPort(req, channel);
        Message res = null;
        if (StringUtils.isEmpty(sendType)) {
            sendType = Constants.SYNC_KEY;
        }
        if (sendType.equals(Constants.SYNC_KEY)) {
            RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);
            res = response.getMessage();
        } else if (sendType.equals(Constants.ASYNC_KEY)) {
            ResponseFuture future = this.invokeAsyncImpl(channel, request, timeoutMillis, null);
            res = new Message();
            RpcContext.getContext().setFuture(future);
        } else if (sendType.equals(Constants.ONE_WAY_KEY)) {
            this.invokeOnewayImpl(channel, request, timeoutMillis);
            res = new Message();
        }
        return res;
    }

    private void setHostAndPort(Message req, Channel channel) {
//        String host = "";
//        int port = 0;
//        if (channel.localAddress() instanceof InetSocketAddress) {
//            host = ((InetSocketAddress) channel.localAddress()).getAddress().toString();
//            port = ((InetSocketAddress) channel.localAddress()).getPort();
//        }

//        req.metadata.put("_host", host);
//        req.metadata.put("_port", String.valueOf(port));
    }


    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }
        return this.createChannel(addr);
    }


    private Channel createChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                cw = this.channelTables.get(addr);
                if (cw != null) {
                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }
                //需要创建新的连接
                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(string2SocketAddress(addr));
                    logger.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                    logger.info("createChannel: connect remote host[{}] success, {}", addr, channelFuture.toString());
                    return cw.getChannel();
                } else {
                    logger.warn("createChannel: connect remote host[" + addr + "] failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                logger.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.nettyClientConfig.getConnectTimeoutMillis(),
                        channelFuture.toString());
            }
        }
        return null;
    }


    public static SocketAddress string2SocketAddress(final String addr) {
        String[] s = addr.split(":");
        InetSocketAddress isa = new InetSocketAddress(s[0], Integer.parseInt(s[1]));
        return isa;
    }

    public void invokeOnewayImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        request.markOnewayRPC();
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) {
                        once.release();//* 释放一次限流次数
                        if (!f.isSuccess()) {
                            logger.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                logger.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                String info = String.format(
                        "invokeOnewayImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                        timeoutMillis,
                        this.semaphoreAsync.getQueueLength(),
                        this.semaphoreAsync.availablePermits()
                );
                logger.warn(info);
                throw new RemotingTimeoutException(info);
            }
        }
    }


    /**
     * 异步调用
     *
     * @param channel
     * @param request
     * @param timeoutMillis
     * @param invokeCallback
     * @return
     * @throws InterruptedException
     * @throws RemotingTooMuchRequestException
     * @throws RemotingSendRequestException
     */
    public ResponseFuture invokeAsyncImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis,
                                          final InvokeCallback invokeCallback)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingSendRequestException {
        final int opaque = request.getOpaque();
        //达到限流作用
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            //用来保证只释放1次的
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, invokeCallback, once);
            this.responseTable.put(opaque, responseFuture);
            try {
                channel.writeAndFlush(request).addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }
                    //发送失败会解除阻塞
                    responseFuture.putResponse(null);
                    responseTable.remove(opaque);
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Throwable e) {
                        logger.warn("excute callback in writeAndFlush addListener, and callback throw", e);
                    } finally {
                        responseFuture.release();
                    }

                    logger.warn("send a request command to channel <{}> failed.", RemotingHelper.parseChannelRemoteAddr(channel));
                });
            } catch (Exception e) {
                responseFuture.release();
                logger.warn("send a request command to channel <" + RemotingHelper.parseChannelRemoteAddr(channel) + "> Exception", e);
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
            return responseFuture;
        } else {
            String info =
                    String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                            timeoutMillis,
                            this.semaphoreAsync.getQueueLength(),
                            this.semaphoreAsync.availablePermits()
                    );
            logger.warn(info);
            throw new RemotingTooMuchRequestException(info);
        }
    }


    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        final int opaque = request.getOpaque();

        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            final SocketAddress addr = channel.remoteAddress();
            //通过网络发送信息
            channel.writeAndFlush(request).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    responseFuture.setSendRequestOK(true);
                    return;
                } else {
                    responseFuture.setSendRequestOK(false);
                }
                //发送失败responseTable中直接删除
                responseTable.remove(opaque);
                responseFuture.setCause(f.cause());
                responseFuture.putResponse(null);
                logger.warn("send a request command to channel <" + addr + "> failed.");
            });
            //这里会直接阻塞住(CountDownLatch)
            RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(addr.toString(), timeoutMillis,
                            responseFuture.getCause());
                } else {
                    throw new RemotingSendRequestException(addr.toString(), responseFuture.getCause());
                }
            }
            return responseCommand;
        } finally {
            this.responseTable.remove(opaque);
        }
    }


    @Override
    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecuter.putNettyEvent(event);
    }

    @Override
    public IServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    @Override
    public void close() {
        logger.info("netty client close");
        if (null != this.eventLoopGroupWorker) {
            this.eventLoopGroupWorker.shutdownGracefully();
        }

        if (this.nettyEventExecuter != null) {
            this.nettyEventExecuter.shutdown();
        }

        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }

        if (null != this.serviceDiscovery) {
            this.serviceDiscovery.close();
        }
    }
}
