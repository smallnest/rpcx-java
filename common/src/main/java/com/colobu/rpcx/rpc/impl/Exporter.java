package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.Config;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.annotation.Provider;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 */
public class Exporter {

    public Set<String> export(String providerPackage) {
        Set<Class<?>> set = new ProviderFinder(providerPackage).find();
        return set.stream().map(it -> {
            String name = it.getName();
            URL url = new URL("rpcx", "", 0);
            url.setPath(name);
            //类名称
            Provider provider = it.getAnnotation(Provider.class);
            //抽取出权重
            String weight = provider.weight();
            String weightValue = Config.ins().get(weight);
            if (null == weightValue) {
                weightValue = "1";
            }
            //导出的时候需要确定权重
            url = url.addParameter("weight", weightValue);
            return url.toFullString();
        }).collect(Collectors.toSet());
    }
}
