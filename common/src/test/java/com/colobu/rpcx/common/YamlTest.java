package com.colobu.rpcx.common;

import com.colobu.rpcx.config.RpcxConfig;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class YamlTest {


    @Test
    public void testYaml() {
        String yamlStr = "key: hello yaml";
        Yaml yaml = new Yaml();
        Map<String, String> ret = yaml.load(yamlStr);
        System.out.println(ret);
        System.out.println(ret.get("key"));
    }


    @Test
    public void testDump() {
        System.out.println(dump());
    }

    private String dump() {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        RpcxConfig config = new RpcxConfig();
        config.setConsumerPackage("com.test.consumer");
        config.setFilterPackage("com.test.filter");
        return yaml.dump(config);
    }


    @Test
    public void testLoad() {
        Yaml yaml = new Yaml();
        RpcxConfig config = yaml.load(dump());
        System.out.println(config.getConsumerPackage());
    }

}
