package com.colobu.rpcx.selector;

public class Weighted {

    public String server;
    public int weight;
    public int currentWeight;
    public int effectiveWeight;

    public Weighted(String server, int weight) {
        this.server = server;
        this.weight = weight;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public int getEffectiveWeight() {
        return effectiveWeight;
    }

    public void setEffectiveWeight(int effectiveWeight) {
        this.effectiveWeight = effectiveWeight;
    }

}
