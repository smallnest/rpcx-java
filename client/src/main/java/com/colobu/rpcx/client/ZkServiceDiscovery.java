package com.colobu.rpcx.client;

import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.rpc.impl.ConsumerFinder;
import com.colobu.rpcx.utils.PathStatus;
import com.colobu.rpcx.utils.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 */
public class ZkServiceDiscovery implements IServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    private ConcurrentHashMap<String, Set<Pair<String, String>>> map = new ConcurrentHashMap<>();

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
            Set<Pair<String, String>> set = ZkClient.ins().get(basePath, it).stream().map(it2 -> {
                String addr = it2.getObject1().split("@")[1];
                return Pair.of(addr, it2.getObject2());
            }).collect(Collectors.toSet());
            this.map.put(it, set);
        });

        try {
            watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * golang 的服务发现
     */
    public ZkServiceDiscovery(final String basePath, final String... serviceName) {
        this.basePath = basePath;
        Arrays.stream(serviceName).forEach(it -> {
            this.serviceName.add(it);
        });
        logger.info("consumer:{}", this.map);

        //直接获取服务信息
        this.serviceName.stream().forEach(it -> {
            Set<Pair<String, String>> set = ZkClient.ins().get(basePath, it);
            this.map.put(it, set.stream().map(it1 -> {
                String addr = it1.getObject1();
                return Pair.of(addr.split("@")[1], it1.getObject2());
            }).collect(Collectors.toSet()));
        });


        try {
            watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<String> getServices(String serviceName) {
        return this.map.get(serviceName).stream().map(it -> it.getObject1() + "?" + it.getObject2()).collect(Collectors.toList());
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
                        } else if ("CHILD_UPDATED".equals(ps.getType())) {
                            this.map.compute(service, (k, v) -> {
                                v = v.stream().map(it -> {
                                    if (it.getObject1().equals(ps.getValue().getObject1())) {
                                        it.setObject2(ps.getValue().getObject2());
                                        return it;
                                    }
                                    return it;
                                }).collect(Collectors.toSet());
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
        stop.compareAndSet(false, true);
    }
}
