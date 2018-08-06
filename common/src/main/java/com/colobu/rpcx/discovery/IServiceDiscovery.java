package com.colobu.rpcx.discovery;

import java.util.List;

/**
 * @author goodjava@qq.com
 */
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

    void close();
}
