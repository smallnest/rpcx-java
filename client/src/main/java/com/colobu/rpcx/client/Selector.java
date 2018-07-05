package com.colobu.rpcx.client;

import java.util.List;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public interface Selector {


    String select(String servicePath, String serviceMethod, List<String> serviceList);

}
