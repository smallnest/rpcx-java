package com.colobu.rpcx.selector;

public enum SelectMode {
    /**
     * RandomSelect is selecting randomly
     */
    RandomSelect,
    /**
     * RoundRobin is selecting by round robin
     */
    RoundRobin,
    /**
     * WeightedRoundRobin is selecting by weighted round robin
     */
    WeightedRoundRobin,
    /**
     * SelectByUser is selecting by implementation of users
     */
    SelectByUser,
}
