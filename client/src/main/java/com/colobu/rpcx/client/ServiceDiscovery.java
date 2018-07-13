package com.colobu.rpcx.client;

import com.colobu.rpcx.utils.PathStatus;
import com.colobu.rpcx.utils.ZkClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ServiceDiscovery {

    private ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<>();

    private final String basePath;
    private final String serviceName;

    public ServiceDiscovery(final String basePath, final String serviceName) {
        this.basePath = basePath;
        this.serviceName = serviceName;
        this.map.put(serviceName, new HashSet<>());

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            System.out.println(this.map);
        }, 0, 5, TimeUnit.SECONDS);
        try {
            watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getServices() {
        return this.map.get("com.colobu.rpcx.service.Arith").stream().collect(Collectors.toList());
    }


    public void watch() throws Exception {
        LinkedBlockingQueue<PathStatus> queue = new LinkedBlockingQueue<>();
        new Thread(() -> {
            while (true) {
                try {
                    PathStatus ps = queue.take();
                    System.out.println("---------->" + ps);

                    if (ps.getType().equals("CHILD_ADDED")) {
                        this.map.compute(serviceName, (k, v) -> {
                            v.add(ps.getValue());
                            return v;
                        });
                    } else if (ps.getType().equals("CHILD_REMOVED")) {
                        this.map.compute(serviceName, (k, v) -> {
                            v.remove(ps.getValue());
                            return v;
                        });
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        ZkClient.ins().watch(queue, basePath + serviceName);
    }
}
