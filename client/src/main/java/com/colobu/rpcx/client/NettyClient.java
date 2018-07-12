package com.colobu.rpcx.client;

import com.colobu.rpcx.client.impl.RandomSelector;
import com.colobu.rpcx.config.NettyClientConfig;
import com.colobu.rpcx.exception.RemotingSendRequestException;
import com.colobu.rpcx.exception.RemotingTimeoutException;
import com.colobu.rpcx.netty.*;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public class NettyClient extends NettyRemotingAbstract implements IClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private NettyClientConfig nettyClientConfig = new NettyClientConfig();

    private final EventLoopGroup eventLoopGroupWorker;



    protected final NettyEventExecuter nettyEventExecuter = new NettyEventExecuter();



    private Bootstrap bootstrap;

    private final ServiceDiscovery serviceDiscovery;

    public NettyClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(//
                nettyClientConfig.getClientWorkerThreads(), //
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                    }
                });


        this.bootstrap = new Bootstrap().group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)//
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyClientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, nettyClientConfig.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, nettyClientConfig.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new NettyEncoder(),
                                new NettyDecoderBak2(),
                                new IdleStateHandler(0, 0, nettyClientConfig.getClientChannelMaxIdleTimeSeconds()), //*空闲状态的handler
                                new NettyConnetManageHandler(NettyClient.this), //管理连接的
                                new NettyClientHandler(NettyClient.this));//* 处理具体业务逻辑的handler
                    }
                });

        Timer timer = new Timer("ClientHouseKeepingService", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    NettyClient.this.scanResponseTable();//* 查询response表
                } catch (Exception e) {
                    logger.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);

        if (this.channelEventListener != null) {//* netty 事件的处理
            this.nettyEventExecuter.run();
        }
    }





    /**
     * 同步的调用
     * @param addr
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    public Message call(String addr, Message req) throws Exception {
        System.out.println("****************call");
        final RemotingCommand request = RemotingCommand.createRequestCommand(1);
        request.setMessage(req);
        long timeoutMillis = 300000000;//TODO $--
        final Channel channel = this.getAndCreateChannel(addr);//* 获取或者创建channel
        RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);//* 同步执行
        return response.getMessage();
    }


    public Message call(Message req) throws Exception {
        final RemotingCommand request = RemotingCommand.createRequestCommand(1);
        request.setMessage(req);
        long timeoutMillis = 300000000;//TODO $--

        List<String> serviceList = this.serviceDiscovery.getServices();
        String addr = new RandomSelector().select(req.servicePath, req.serviceMethod,serviceList);
        if (null == addr) {
            throw new RuntimeException("addr error");
        }
        final Channel channel = this.getAndCreateChannel(addr);//* 获取或者创建channel
        RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);//* 同步执行
        return response.getMessage();
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


    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        final int opaque = request.getOpaque();

        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            final SocketAddress addr = channel.remoteAddress();

            System.out.println("00000000000000000");

            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {//* 通过网络发送信息
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(opaque);//* 发送失败responseTable中直接删除
                    responseFuture.setCause(f.cause());
                    responseFuture.putResponse(null);
                    logger.warn("send a request command to channel <" + addr + "> failed.");
                }
            });

            RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);//* 这里会直接阻塞住(CountDownLatch)
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




    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecuter.putNettyEvent(event);
    }
}
