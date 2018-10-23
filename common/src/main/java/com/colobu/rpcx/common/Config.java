package com.colobu.rpcx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Properties;

/**
 * @author goodjava@qq.com
 */
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private Properties properties;

    private Config() {
        properties = new ClassPathResource("application.properties").getProperties();
        //拥有多个配置文件
        if (properties.containsKey("spring.profiles.active")) {
            Properties propertiesTmp = new ClassPathResource("application-" + properties.get("spring.profiles.active") + ".properties").getProperties();
            propertiesTmp.keySet().stream().forEach(key -> properties.put(key, propertiesTmp.get(key)));
        }
        String yamlProperties = new ClassPathResource("application.yaml").getString();
        if (StringUtils.isNotEmpty(yamlProperties)) {
            Yaml yaml = new Yaml();
            Map<String, String> m = yaml.load(yamlProperties);
            m.entrySet().stream().forEach(kv -> {
                if (kv.getKey().startsWith("rpcx.")) {
                    properties.put(kv.getKey(), String.valueOf(kv.getValue()));
                }
            });
        }
        logger.info("read config finish :{}", this.properties.keySet());
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

    public String get(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }
}
