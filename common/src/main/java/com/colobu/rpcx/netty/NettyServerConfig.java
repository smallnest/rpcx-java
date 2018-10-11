package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.Config;

/**
 * @author goodjava@qq.com
 */
public class NettyServerConfig implements Cloneable {
    private int listenPort = 8888;
    private int serverWorkerThreads = Runtime.getRuntime().availableProcessors() * 2;
    private int serverCallbackExecutorThreads = 0;
    private int serverOnewaySemaphoreValue = 256;
    private int serverAsyncSemaphoreValue = 64;
    private int serverChannelMaxIdleTimeSeconds = 120;

    private int serverSocketSndBufSize = 1024 * 64;
    private int serverSocketRcvBufSize = 1024 * 64;

    //业务的线程数
    private int serverBizThreads = 1000;


    public int getServerBizThreads() {
        int num = Integer.valueOf(Config.ins().get("rpcx.biz.processor.num", String.valueOf(serverBizThreads)));
        return num;
    }

    public int getListenPort() {
        return listenPort;
    }


    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }


    public int getServerWorkerThreads() {
        int num = Integer.valueOf(Config.ins().get("rpcx.worker.processor.num", String.valueOf(serverWorkerThreads)));
        return num;
    }


    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }


    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }


    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }


    public int getServerCallbackExecutorThreads() {
        return serverCallbackExecutorThreads;
    }


    public void setServerCallbackExecutorThreads(int serverCallbackExecutorThreads) {
        this.serverCallbackExecutorThreads = serverCallbackExecutorThreads;
    }


    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }


    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }


    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }


    public void setServerChannelMaxIdleTimeSeconds(int serverChannelMaxIdleTimeSeconds) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
    }


    public int getServerSocketSndBufSize() {
        int size = Integer.valueOf(Config.ins().get("rpcx.snd.buf.size", String.valueOf(serverSocketSndBufSize)));
        return size;
    }


    public int getServerSocketRcvBufSize() {
        int size = Integer.valueOf(Config.ins().get("rpcx.rcv.buf.size", String.valueOf(serverSocketRcvBufSize)));
        return size;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return (NettyServerConfig) super.clone();
    }

}
