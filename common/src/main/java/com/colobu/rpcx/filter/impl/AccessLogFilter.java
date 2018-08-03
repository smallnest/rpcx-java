package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.common.NamedThreadFactory;
import com.colobu.rpcx.concurrent.ConcurrentHashSet;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author goodjava@qq.com
 */
@RpcFilter(group = {Constants.PROVIDER},order = -999)
public class AccessLogFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

    private static final String FILE_DATE_FORMAT = "yyyyMMdd";

    private static final String MESSAGE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final int LOG_MAX_BUFFER = 5000;

    private static final long LOG_OUTPUT_INTERVAL = 5000;

    private final static ConcurrentMap<String, Set<String>> LOG_QUEUE = new ConcurrentHashMap<>();

    private final static ScheduledExecutorService LOG_SCHEDULED = Executors.newScheduledThreadPool(2, new NamedThreadFactory("Rcpx-Access-Log", true));

    private volatile ScheduledFuture<?> logFuture = null;

    /**
     * 非阻塞
     */
    private class LogTask implements Runnable {
        @Override
        public void run() {
            try {
                if (LOG_QUEUE != null && LOG_QUEUE.size() > 0) {
                    for (Map.Entry<String, Set<String>> entry : LOG_QUEUE.entrySet()) {
                        try {
                            String accesslog = entry.getKey();
                            Set<String> logSet = entry.getValue();
                            File file = new File(accesslog);
                            File dir = file.getParentFile();
                            if (null != dir && !dir.exists()) {
                                dir.mkdirs();
                            }
                            if (file.exists()) {
                                String now = new SimpleDateFormat(FILE_DATE_FORMAT).format(new Date());
                                String last = new SimpleDateFormat(FILE_DATE_FORMAT).format(new Date(file.lastModified()));
                                if (!now.equals(last)) {
                                    File archive = new File(file.getAbsolutePath() + "." + last);
                                    file.renameTo(archive);
                                }
                            }
                            FileWriter writer = new FileWriter(file, true);
                            try {
                                for (Iterator<String> iterator = logSet.iterator();
                                     iterator.hasNext();
                                     iterator.remove()) {
                                    writer.write(iterator.next());
                                    writer.write("\r\n");
                                }
                                writer.flush();
                            } finally {
                                writer.close();
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void init() {
        if (logFuture == null) {
            synchronized (LOG_SCHEDULED) {
                if (logFuture == null) {
                    logFuture = LOG_SCHEDULED.scheduleWithFixedDelay(new LogTask(), LOG_OUTPUT_INTERVAL, LOG_OUTPUT_INTERVAL, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void log(String accesslog, String message) {
        init();
        Set<String> logSet = LOG_QUEUE.get(accesslog);
        if (logSet == null) {
            LOG_QUEUE.putIfAbsent(accesslog, new ConcurrentHashSet<>());
            logSet = LOG_QUEUE.get(accesslog);
        }
        if (logSet.size() < LOG_MAX_BUFFER) {
            logSet.add(message);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation inv) throws RpcException {
        try {
            RpcContext context = RpcContext.getContext();
            String serviceName = invoker.getInterface().getName();
            String version = invoker.getUrl().getParameter(Constants.VERSION_KEY);
            String group = invoker.getUrl().getParameter(Constants.GROUP_KEY);
            StringBuilder sn = new StringBuilder();
            sn.append("[").append(new SimpleDateFormat(MESSAGE_DATE_FORMAT).format(new Date())).append("] ").append(context.getRemoteHost()).append(":").append(context.getRemotePort())
                    .append(" -> ").append(context.getLocalHost()).append(":").append(context.getLocalPort())
                    .append(" - ");
            if (null != group && group.length() > 0) {
                sn.append(group).append("/");
            }
            sn.append(serviceName);
            if (null != version && version.length() > 0) {
                sn.append(":").append(version);
            }
            sn.append(" ");
            sn.append(inv.getMethodName());
            sn.append("(");
            Class<?>[] types = inv.getParameterTypes();
            if (types != null && types.length > 0) {
                boolean first = true;
                for (Class<?> type : types) {
                    if (first) {
                        first = false;
                    } else {
                        sn.append(",");
                    }
                    sn.append(type.getName());
                }
            }
            sn.append(") ");
            Object[] args = inv.getArguments();
            if (args != null && args.length > 0) {
                sn.append(new Gson().toJson(args));
            }
            String msg = sn.toString();
            log("accesslog", msg);
        } catch (Throwable t) {
            logger.warn("Exception in AcessLogFilter of service(" + invoker + " -> " + inv + ")", t);
        }
        return invoker.invoke(inv);
    }

}