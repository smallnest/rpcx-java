package com.colobu.rpcx.client;

import java.util.List;

public interface IServiceDiscovery {

    /**
     * 根据serviceName 获取 服务的ip和端口号列表
     * @param serviceName
     * @return
     */
    List<String> getServices(String serviceName);

    /**
     * 观察注册中心的变化
     */
    void watch();

}
