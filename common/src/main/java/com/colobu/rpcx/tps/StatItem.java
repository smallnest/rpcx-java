package com.colobu.rpcx.tps;


import java.util.concurrent.atomic.AtomicInteger;

class StatItem {

    private String name;

    private long lastResetTime;

    private long interval;

    private AtomicInteger token;

    private int rate;

    StatItem(String name, int rate, long interval) {
        this.name = name;
        this.rate = rate;
        this.interval = interval;
        this.lastResetTime = System.currentTimeMillis();
        this.token = new AtomicInteger(rate);
    }

    public boolean isAllowable() {
        long now = System.currentTimeMillis();
        if (now > lastResetTime + interval) {//如果超过了限流时间,则数量和时间和rate都重置
            token.set(rate);
            lastResetTime = now;
        }

        int value = token.get();
        boolean flag = false;
        while (value > 0 && !flag) {//如果小于0了则直接返回false
            flag = token.compareAndSet(value, value - 1);
            value = token.get();
        }

        return flag;
    }

    long getLastResetTime() {
        return lastResetTime;
    }
    
    int getToken() {
        return token.get();
    }
    
    @Override
    public String toString() {
        return new StringBuilder(32).append("StatItem ")
            .append("[name=").append(name).append(", ")
            .append("rate = ").append(rate).append(", ")
            .append("interval = ").append(interval).append("]")
            .toString();
    }

}
