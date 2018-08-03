package com.colobu.rpcx.client;

import com.colobu.rpcx.common.NamedThreadFactory;
import com.colobu.rpcx.rpc.impl.ConsumerFinder;
import com.colobu.rpcx.utils.PathStatus;
import com.colobu.rpcx.utils.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by goodjava@qq.com.
 */
public class ZkServiceDiscovery implements IServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    private ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<>();

    private final String basePath;

    private Set<String> serviceName = new HashSet<>();

    private LinkedBlockingQueue<PathStatus> queue = new LinkedBlockingQueue<>();

    private Set<String> findConsumer() {
        return new ConsumerFinder().find();
    }

    private AtomicBoolean stop = new AtomicBoolean(false);

    /**
     * java 的服务发现,会查找所有的consumer
     *
     * @param basePath
     */
    public ZkServiceDiscovery(final String basePath) {
        this.basePath = basePath;
        this.serviceName = findConsumer();
        this.serviceName.stream().forEach(it -> {
            this.map.put(it, new HashSet<>());
        });

        this.serviceName.stream().forEach(it -> {
            Set<String> set = ZkClient.ins().get(basePath, it).stream().map(it2 -> it2.split("@")[1]).collect(Collectors.toSet());
            this.map.put(it, set);
        });

        try {
            watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //golang 的服务发现
    public ZkServiceDiscovery(final String basePath, final String serviceName) {
        this.basePath = basePath;
        this.serviceName.add(serviceName);
        logger.info("consumer:{}", this.map);

        //直接获取服务信息
        this.serviceName.stream().forEach(it -> {
            Set<String> set = ZkClient.ins().get(basePath, it);
            this.map.put(it, set.stream().map(it1 -> it1.split("@")[1]).collect(Collectors.toSet()));
        });


        new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("provider_info")).scheduleWithFixedDelay(() -> {
            logger.info("provider info:{}", this.map);
        }, 0, 5, TimeUnit.SECONDS);
        try {
            watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<String> getServices(String serviceName) {
        return this.map.get(serviceName).stream().collect(Collectors.toList());
    }


    @Override
    public void watch() {
        new Thread(() -> {
            while (true) {
                try {
                    PathStatus ps = queue.take();
                    if (ps.isStop()) {
                        break;
                    }
                    logger.info("=========>zk discovery ps:{}", ps);
                    String service = ps.getPath().replace(this.basePath, "");

                    if (this.map.containsKey(service)) {
                        if ("CHILD_ADDED".equals(ps.getType())) {
                            this.map.compute(service, (k, v) -> {
                                v.add(ps.getValue());
                                return v;
                            });
                        } else if ("CHILD_REMOVED".equals(ps.getType())) {
                            this.map.compute(service, (k, v) -> {
                                v.remove(ps.getValue());
                                return v;
                            });
                        }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        this.serviceName.stream().forEach(it -> {
            try {
                ZkClient.ins().watch(queue, basePath + it);
            } catch (Exception e) {
                logger.error("service name foreach {} error:{}", it, e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        this.queue.offer(new PathStatus(true));
        ZkClient.ins().close();
        stop.compareAndSet(false,true);
    }
}
