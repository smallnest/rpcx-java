package com.colobu.rpcx.common;

import java.util.Properties;

//配置读取
public class Config {

    private Properties properties;

    private Config() {
        properties = new ClassPathResource("application.properties").getProperties();
    }


    private static class LazyHolder {
        private static final Config ins = new Config();
    }


    public static Config ins() {
        return LazyHolder.ins;
    }

    public String get(String key) {
        return this.properties.getProperty(key);
    }
}
