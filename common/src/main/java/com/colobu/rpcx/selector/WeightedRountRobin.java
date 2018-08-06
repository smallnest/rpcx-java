package com.colobu.rpcx.selector;

/**
 * @author goodjava@qq.com
 */
public class WeightedRountRobin {

    private Weighted[] weighteds;

    public WeightedRountRobin(Weighted[] weighteds) {
        this.weighteds = weighteds;
    }

    public Weighted nextWeighted() {
        Weighted best = null;

        int total = 0;

        for (int i = 0; i < weighteds.length; i++) {
            Weighted w = weighteds[i];

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
