package com.colobu.rpcx.server;

import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.common.RemotingUtil;
import com.colobu.rpcx.netty.*;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class NettyServer extends NettyRemotingAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final ServerBootstrap serverBootstrap;

    @Getter
    private String addr;

    @Getter
    private int port;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final Timer timer = new Timer("ServerHouseKeepingService", true);


    private final EventLoopGroup eventLoopGroupBoss;
    private final EventLoopGroup eventLoopGroupSelector;
    private final NettyServerConfig nettyServerConfig;

    public NettyServer() {
        nettyServerConfig = new NettyServerConfig();
        this.serverBootstrap = new ServerBootstrap();

        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
            }
        });

        this.eventLoopGroupSelector = new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            private int threadTotal = nettyServerConfig.getServerSelectorThreads();


            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
            }
        });

    }


    public void start() {
        //默认的处理器
        this.defaultRequestProcessor = new Pair<>(new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
                System.out.println(request);
                System.out.println(new String(request.getMessage().payload));

                Message reqMsg = request.getMessage();
                String method = reqMsg.serviceMethod;
                String clazz = reqMsg.servicePath;

                Class<?> c = Class.forName("com.colobu.rpcx.service." + clazz);
                Object obj = c.newInstance();

                Method m = c.getMethod(method, new byte[]{}.getClass());
                Object v = m.invoke(obj, reqMsg.payload);

                RemotingCommand res = RemotingCommand.createResponseCommand();
                Message message = new Message();
                message.setMessageType(MessageType.Response);
                message.setSeq(request.getOpaque());
                message.payload = (byte[]) v;
                res.setMessage(message);
                return res;
            }

            @Override
            public boolean rejectRequest() {
                return false;
            }
        }, Executors.newFixedThreadPool(10));


        DefaultEventExecutorGroup defaultEventExecutorGroup = new DefaultEventExecutorGroup(//
                nettyServerConfig.getServerWorkerThreads(), //
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
                    }
                });


        ServerBootstrap childHandler = //
                this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector).channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_KEEPALIVE, false)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
                        .option(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
                        .localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(
                                        defaultEventExecutorGroup, //
                                        new NettyEncoder(), //
                                        new NettyDecoder(), //
                                        new IdleStateHandler(0, 0, nettyServerConfig.getServerChannelMaxIdleTimeSeconds()), //
                                        new NettyConnetManageHandler(NettyServer.this), //
                                        new NettyServerHandler(NettyServer.this));
                            }
                        });

        if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
            childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            String inetHost = RemotingUtil.getLocalAddress();
            ChannelFuture sync = this.serverBootstrap.bind(inetHost, 0).sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            this.port = addr.getPort();
            this.addr = addr.getHostString();
            logger.info("rpc server addr:{} port:{}", this.addr, this.port);
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }

        if (this.channelEventListener != null) {
            this.nettyEventExecuter.run();
        }

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                } catch (Exception e) {
                    NettyServer.this.scanResponseTable();
                    logger.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("shutdown begin");
            latch.countDown();
            logger.info("shutdown end");
        }));

    }


    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
