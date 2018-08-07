package com.colobu.rpcx.selector;

import com.colobu.rpcx.rpc.URL;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 * <p>
 * https://github.com/phusion/nginx/commit/27e94984486058d73157038f7950a0a36ecc6e35
 */
public class WeightedRountRobin {

    private List<Weighted> weighteds;

    public WeightedRountRobin(Weighted[] weighteds) {
        this.weighteds = Arrays.asList(weighteds);
    }


    private boolean contains(URL url) {
        String weight = url.getParameter("weight");
        String address = url.getAddress();

        return weighteds.stream().anyMatch((it) -> {
            if (it.getServer().equals(address) && it.getWeight() == Integer.parseInt(weight)) {
                return true;
            }
            return false;
        });

    }


    private boolean contains(List<String> serverList, Weighted weighted) {
        return serverList.stream().anyMatch(it -> {
            URL url = URL.valueOf(it);
            if (url.getAddress().equals(weighted.server) && url.getParameter("weight").equals(String.valueOf(weighted.weight))) {
                return true;
            }
            return false;
        });
    }


    /**
     * 有可能会有服务器下线或上线
     *
     * @param serviceList
     */
    public void updateWeighteds(List<String> serviceList) {

        this.weighteds = weighteds.stream().filter(it -> {
            return contains(serviceList, it);
        }).collect(Collectors.toList());

        serviceList.stream().forEach(it -> {
            URL url = URL.valueOf(it);
            if (!contains(url)) {
                weighteds.add(new Weighted(url.getAddress(), Integer.parseInt(url.getParameter("weight"))));
            }
        });

    }


    public WeightedRountRobin(List<String> serviceList) {
        Weighted[] weighteds = serviceList.stream().map(it -> {
            URL url = URL.valueOf(it);
            Weighted weighted = new Weighted(url.getAddress(), Integer.parseInt(url.getParameter("weight")));
            return weighted;
        }).toArray(Weighted[]::new);
        this.weighteds = Arrays.asList(weighteds);
    }

    public Weighted nextWeighted() {
        Weighted best = null;

        int total = 0;

        for (int i = 0; i < weighteds.size(); i++) {
            Weighted w = weighteds.get(i);

            if (w == null) {
                continue;
            }
            //if w is down, continue

            w.currentWeight += w.effectiveWeight;
            total += w.effectiveWeight;
            if (w.effectiveWeight < w.weight) {
                w.effectiveWeight++;
            }

            if (best == null || w.currentWeight > best.currentWeight) {
                best = w;
            }

        }

        if (best == null) {
            return null;
        }

        best.currentWeight -= total;
        return best;
    }
}
