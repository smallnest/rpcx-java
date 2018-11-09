package com.colobu.rpcx.config;

public class RpcxConfig {
    /**
     * consumer的包路径
     */
    private String consumerPackage;
    /**
     * provider 的包路径
     */
    private String providerPackage;
    /**
     * 用户自定义filter 的包路径
     */
    private String filterPackage;
    /**
     * rpcx 连接 zk的地址 host:port
     */
    private String zkAddr;
    /**
     * 热更新的token
     */
    private String hotDeployToken;
    /**
     * 服务的端口号
     */
    private int serverPort;

    /**
     * 业务处理的线程数量
     */
    private int bizProcessorNum;

    /**
     * 服务器read write的线程数量
     */
    private int workerProcessorNum;

    private int sndBufSize;

    private int rcvBufSize;


    public String getConsumerPackage() {
        return consumerPackage;
    }

    public void setConsumerPackage(String consumerPackage) {
        this.consumerPackage = consumerPackage;
    }

    public String getProviderPackage() {
        return providerPackage;
    }

    public void setProviderPackage(String providerPackage) {
        this.providerPackage = providerPackage;
    }

    public String getFilterPackage() {
        return filterPackage;
    }

    public void setFilterPackage(String filterPackage) {
        this.filterPackage = filterPackage;
    }

    public String getZkAddr() {
        return zkAddr;
    }

    public void setZkAddr(String zkAddr) {
        this.zkAddr = zkAddr;
    }

    public String getHotDeployToken() {
        return hotDeployToken;
    }

    public void setHotDeployToken(String hotDeployToken) {
        this.hotDeployToken = hotDeployToken;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


    public int getBizProcessorNum() {
        return bizProcessorNum;
    }

    public void setBizProcessorNum(int bizProcessorNum) {
        this.bizProcessorNum = bizProcessorNum;
    }

    public int getWorkerProcessorNum() {
        return workerProcessorNum;
    }

    public void setWorkerProcessorNum(int workerProcessorNum) {
        this.workerProcessorNum = workerProcessorNum;
    }

    public int getSndBufSize() {
        return sndBufSize;
    }

    public void setSndBufSize(int sndBufSize) {
        this.sndBufSize = sndBufSize;
    }

    public int getRcvBufSize() {
        return rcvBufSize;
    }

    public void setRcvBufSize(int rcvBufSize) {
        this.rcvBufSize = rcvBufSize;
    }
}
