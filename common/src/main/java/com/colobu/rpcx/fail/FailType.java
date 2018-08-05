package com.colobu.rpcx.fail;

/**
 * @author goodjava@qq.com
 */
public enum FailType {
    /**
     * 立刻返回
     */
    FailFast,
    /**
     * 换一个服务节点
     */
    FailOver,
    /**
     * 在当前节点重试
     */
    FailTry,
}
