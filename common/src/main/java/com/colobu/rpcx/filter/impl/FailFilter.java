package com.colobu.rpcx.filter.impl;

import com.colobu.rpcx.common.retry.RetryNTimes;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.selector.SelectMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author goodjava@qq.com
 * <p>
 * 负责管理失败的重试
 */
@RpcFilter(group = {Constants.CONSUMER}, order = -1000)
public class FailFilter implements Filter {


    private static final Logger logger = LoggerFactory.getLogger(FailFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        FailType failType = invocation.getFailType();
        RetryNTimes retry = new RetryNTimes(invocation.getRetryNum());
        Result result = retry.retry((n) -> {
            Result res = invoker.invoke(invocation);
            if (res.hasException()) {
                logger.warn("{} invoke error:{}", invocation.getUrl().getPath(), res.getException().getMessage());
                //失败了立刻返回
                if (failType.equals(FailType.FailFast)) {
                    logger.info("------>failFast");
                    res.getAttachments().put("needRetry", "false");
                    return res;
                }
                //换个服务节点重试
                if (failType.equals(FailType.FailOver)) {
                    logger.info("------>failOver");
                    res.getAttachments().put("needRetry", "true");
                    return res;
                }
                //当前节点重试
                if (failType.equals(FailType.FailTry)) {
                    logger.info("------>failTry");
                    res.getAttachments().put("needRetry", "true");
                    //这样走到下游的SelectFilter就不会再选出来一个新的Service了
                    invocation.setSelectMode(SelectMode.SelectByUser);
                    return res;
                }
            }
            res.getAttachments().put("needRetry", "false");
            return res;
        });

        return result;
    }
}
