package com.colobu.rpcx.client;

import java.util.List;

/**
 * Created by goodjava@qq.com.
 */
public interface Selector {


    String select(String servicePath, String serviceMethod, List<String> serviceList);

}
