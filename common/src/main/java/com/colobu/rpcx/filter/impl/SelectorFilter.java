package com.colobu.rpcx.filter.impl;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.selector.SelectMode;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author goodjava@qq.com
 */
@RpcFilter(group = {Constants.CONSUMER})
public class SelectorFilter implements Filter {

    private IServiceDiscovery discovery;

    private static SecureRandom secureRandom = new SecureRandom();

    private static ConcurrentHashMap<String, Integer> selectIndexMap = new ConcurrentHashMap<>();

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        //目前使用类名当服务名称
        String serviceName = invocation.getClassName();

        List<String> serviceList = this.discovery.getServices(serviceName);
        if (serviceList.size() <= 0) {
            throw new RpcException("serviceList size <=0 ", -3);
        }

        SelectMode selectMode = invocation.getSelectMode();
        switch (selectMode) {
            case RandomSelect: {

                break;
            }
            case RoundRobin: {
                break;
            }

            case WeightedRoundRobin:{
                break;
            }
        }


        return invoker.invoke(invocation);
    }


    private String roundRobin(List<String> serviceList, String serviceName) {
        Integer index = selectIndexMap.get(serviceName);
        if (null == index) {
            selectIndexMap.putIfAbsent(serviceName, 0);
        }
        index = selectIndexMap.get(serviceName);

        int i = index % serviceList.size();
        String serviceAddr = serviceList.get(i);
        selectIndexMap.compute(serviceName, (k, v) -> {
            v = v + 1;
            return v;
        });
        return serviceAddr;
    }


    private String weightedRountRobin(List<String> serviceList) {
        return "";
    }




    private String randomSelect(List<String> serviceList) {
        int index = secureRandom.nextInt(serviceList.size());
        return serviceList.get(index);
    }
}
