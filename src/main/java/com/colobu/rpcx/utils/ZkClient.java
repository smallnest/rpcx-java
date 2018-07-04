package com.colobu.rpcx.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ZkClient {

    private CuratorFramework client;

    private ZkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client =
                CuratorFrameworkFactory.builder()
                        .connectString("127.0.0.1:2181")
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .build();


        client.start();
    }

    private static final class LazyHolder {
        private static final ZkClient ins = new ZkClient();
    }

    public static ZkClient ins() {
        return LazyHolder.ins;
    }


    public List<String> getChildren() {
        try {
            List<String> list = client.getChildren().forPath("/youpin/services/Arith").stream().collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void watch() throws Exception {
        final NodeCache cache = new NodeCache(this.client, "/", false);
        cache.start(true);
        cache.getListenable().addListener(() -> {
            System.out.println("路径为：" + cache.getCurrentData().getPath());
            System.out.println("数据为：" + new String(cache.getCurrentData().getData()));
            System.out.println("状态为：" + cache.getCurrentData().getStat());
            System.out.println("---------------------------------------");
        });


    }


    public void watch2(LinkedBlockingQueue<PathStatus> queue, String p) throws Exception {
        PathChildrenCache cache = new PathChildrenCache(this.client, p, true);
        //5 在初始化的时候就进行缓存监听
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener((cf, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    String path = event.getData().getPath();
                    System.out.println("CHILD_ADDED :" + path);
                    String addr = path.split("@")[1];
                    System.out.println(addr);
                    queue.offer(new PathStatus("CHILD_ADDED", addr));
                    break;
                case CHILD_UPDATED:
                    System.out.println("CHILD_UPDATED :" + event.getData().getPath());
                    break;
                case CHILD_REMOVED:
                    String path2 = event.getData().getPath();
                    System.out.println("CHILD_REMOVED :" + path2);
                    String addr2 = path2.split("@")[1];
                    System.out.println(addr2);
                    queue.offer(new PathStatus("CHILD_REMOVED", addr2));
                    break;
                default:
                    break;
            }
        });
    }

}
