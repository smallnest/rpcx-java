package com.colobu.rpcx.server;

import com.colobu.rpcx.common.*;
import com.colobu.rpcx.handler.RpcxProcessHandler;
import com.colobu.rpcx.netty.*;
import com.colobu.rpcx.processor.RpcHttpProcessor;
import com.colobu.rpcx.processor.RpcProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * @author goodjava@qq.com
 */
public class NettyServer extends NettyRemotingAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final ServerBootstrap serverBootstrap;

    private String addr;

    private int port;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("timer"));

    private final ScheduledThreadPoolExecutor processingRequestSchedule = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("processingRequestSchedule"));

    private final EventLoopGroup eventLoopGroupBoss;
    private final EventLoopGroup eventLoopGroupSelector;
    private final NettyServerConfig nettyServerConfig;

    /**
     * 如果使用ioc容器,这里需要注入获取bean的 function
     */
    private Function<Class, Object> getBeanFunc;

    public NettyServer() {
        nettyServerConfig = new NettyServerConfig();
        this.serverBootstrap = new ServerBootstrap();
        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new NamedThreadFactory("NettyBoss_", false));
        this.eventLoopGroupSelector = new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(), new NamedThreadFactory("NettyServerNIOSelector", false));
    }


    public void start() {
        //默认的处理器
        this.defaultRequestProcessor = createDefaultRequestProcessor();
        //http网关请求处理器
        this.registerProcessor(1984, new RpcHttpProcessor(this.getBeanFunc), new ThreadPoolExecutor(50, 50,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory("httpRequestProcessor")));

        DefaultEventExecutorGroup defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyServerConfig.getServerWorkerThreads(), new NamedThreadFactory("NettyServerCodecThread_", false));


        ServerBootstrap childHandler = createServerBootstrap(defaultEventExecutorGroup);

        if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
            childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            String host = RemotingUtil.getLocalAddress();
            String port = getServerPort();
            ChannelFuture sync = this.serverBootstrap.bind(host, Integer.parseInt(port)).sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            this.addr = addr.getHostString();
            this.port = addr.getPort();
            logger.info("###########rpc server start addr{} ", this.addr + ":" + this.port);
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }

        if (this.channelEventListener != null) {
            this.nettyEventExecuter.run();
        }

        runScanResponseTableSchedule();
        runProcessingRequestSchedule();
        addShutdownHook();
    }

    private Pair<NettyRequestProcessor, ExecutorService> createDefaultRequestProcessor() {
        return new Pair<>(new RpcProcessor(this.getBeanFunc), new ThreadPoolExecutor(50, 50,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory("defaultRequestProcessor")));
    }

    private void runScanResponseTableSchedule() {
        this.timer.scheduleAtFixedRate(() -> {
            try {
                NettyServer.this.scanResponseTable();
            } catch (Exception e) {
                logger.error("scanResponseTable exception", e);
            }
        }, 1000, 3000, TimeUnit.MILLISECONDS);
    }

    private void runProcessingRequestSchedule() {
        this.processingRequestSchedule.scheduleAtFixedRate(() -> {
            try {
                scanProcessingRequest();
            } catch (Exception ex) {
                logger.error("runProcessingRequestSchedule error:{}", ex.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private ServerBootstrap createServerBootstrap(DefaultEventExecutorGroup defaultEventExecutorGroup) {
        return this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.SO_LINGER, 10)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
                .localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new RpcxProcessHandler(nettyServerConfig.getServerChannelMaxIdleTimeSeconds(), NettyServer.this)
                        );
                    }
                });
    }

    private String getServerPort() {
        String port = Config.ins().get("rpcx_port");
        if (StringUtils.isEmpty(port)) {
            port = "0";
        }
        return port;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("shutdown begin");

            serverShutdown();
            shutdown();

            latch.countDown();
            logger.info("shutdown end");
        }));
    }

    private void shutdown() {
        this.eventLoopGroupSelector.shutdownGracefully();
        this.eventLoopGroupBoss.shutdownGracefully();
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setGetBeanFunc(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }


    public Function<Class, Object> getGetBeanFunc() {
        return getBeanFunc;
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }
}
