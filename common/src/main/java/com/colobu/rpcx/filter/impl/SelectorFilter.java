package com.colobu.rpcx.filter.impl;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.selector.SelectMode;
import com.colobu.rpcx.selector.Weighted;
import com.colobu.rpcx.selector.WeightedRountRobin;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author goodjava@qq.com
 */
@RpcFilter(group = {Constants.CONSUMER})
public class SelectorFilter implements Filter {

    private static SecureRandom secureRandom = new SecureRandom();

    private static ConcurrentHashMap<String, Integer> selectIndexMap = new ConcurrentHashMap<>();

    /**
     * 基于权重
     */
    private static ConcurrentHashMap<String, WeightedRountRobin> weightRoundRobinMap = new ConcurrentHashMap<>();

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        //目前使用类名当服务名称
        String serviceName = invocation.getClassName();

        //获取服务列表(ip:port?param)
        List<String> serviceList = invoker.serviceDiscovery().getServices(serviceName);

        System.out.println("------------>"+serviceList);

        serviceList = serviceList.stream().filter(it->!it.contains("state=inactive")).collect(Collectors.toList());


        if (serviceList.size() <= 0) {
            throw new RpcException("service list size <=0 ", -3);
        }

        SelectMode selectMode = invocation.getSelectMode();
        switch (selectMode) {
            case RandomSelect: {
                String service = randomSelect(serviceList);
                RpcContext.getContext().setServiceAddr(service);
                break;
            }
            case RoundRobin: {
                String service = roundRobin(serviceList, serviceName);
                RpcContext.getContext().setServiceAddr(service);
                break;
            }
            case WeightedRoundRobin: {
                String service = weightedRountRobin(serviceList, serviceName);
                RpcContext.getContext().setServiceAddr(service);
                break;
            }
            case SelectByUser: {
                break;
            }
        }


        return invoker.invoke(invocation);
    }


    private String roundRobin(List<String> serviceList, String serviceName) {
        Integer index = selectIndexMap.get(serviceName);
        if (null == index) {
            selectIndexMap.putIfAbsent(serviceName, 0);
            index = selectIndexMap.get(serviceName);
        }

        int i = index % serviceList.size();
        String serviceAddr = serviceList.get(i);
        selectIndexMap.compute(serviceName, (k, v) -> {
            v = v + 1;
            return v;
        });
        return URL.valueOf(serviceAddr).getAddress();
    }


    private String weightedRountRobin(List<String> serviceList, String serviceName) {
        WeightedRountRobin weightedRountRobin = weightRoundRobinMap.get(serviceName);
        if (null == weightedRountRobin) {
            weightRoundRobinMap.putIfAbsent(serviceName, new WeightedRountRobin(serviceList));
            weightedRountRobin = weightRoundRobinMap.get(serviceName);
        }
        //需要更新权重列表,有可能服务器上下线
        weightedRountRobin.updateWeighteds(serviceList);

        Weighted wei = weightedRountRobin.nextWeighted();
        if (null == wei) {
            return null;
        }
        return URL.valueOf(wei.getServer()).getAddress();
    }


    private String randomSelect(List<String> serviceList) {
        int index = secureRandom.nextInt(serviceList.size());
        return URL.valueOf(serviceList.get(index)).getAddress();
    }
}
