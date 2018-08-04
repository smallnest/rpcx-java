package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.common.Config;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.deploy.AgentLoader;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author goodjava@qq.com
 * 支持热更新
 */
@RpcFilter(order = -2002, group = {Constants.PROVIDER})
public class HotDeployFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HotDeployFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation inv) throws RpcException {
        if (inv.getMethodName().equals(Constants.$HOT_DEPLOY) && inv.getArguments().length == 3) {
            logger.info("hot deploy begin");
            Object[] arguments = inv.getArguments();

            String className = arguments[0].toString();
            String token = arguments[1].toString();

            String configToken = Config.ins().get("rpcx_deploy_tokey");
            if (StringUtils.isEmpty(configToken) || !token.equals(configToken)) {
                throw new RpcException("token error");
            }

            byte[] classData = (byte[]) arguments[2];
            String uuid = UUID.randomUUID().toString();
            String file = "/tmp/" + uuid;
            String pid = getPid();
            logger.info("className:{} file:{} pid:{} data len:{}", className, file, pid, classData.length);
            try {
                Files.write(Paths.get(file), classData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new AgentLoader().loadAgent(pid, Config.ins().get("rpcx_agent_path"), file + "__" + className);

            try {
                Files.delete(Paths.get(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new RpcResult("Finish");
        }

        return invoker.invoke(inv);
    }


    public String getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        // format: "pid@hostname"
        String name = runtime.getName();
        return name.substring(0, name.indexOf('@'));
    }

}