package com.colobu.rpcx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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


        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path);
        List<File> list = Arrays.stream(file.listFiles())
                .peek(it -> logger.info("yaml config:{}", file.toString()))
                .filter(f -> f.toString().endsWith(".yaml")).collect(Collectors.toList());

        list.stream().forEach(f -> {
            try {
                String str = new String(Files.readAllBytes(f.toPath()));
                Yaml yaml = new Yaml();
                Map<String, String> m = yaml.load(str);
                m.entrySet().stream().forEach(kv -> {
                    if (kv.getKey().startsWith("rpcx.")) {
                        properties.put(kv.getKey(), String.valueOf(kv.getValue()));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        logger.info("config info:{}", this.properties.propertyNames());
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
