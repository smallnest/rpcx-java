package com.colobu.rpcx.config;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author goodjava@qq.com
 */
public class Constants {

    public static final String VERSION_KEY = "version";

    public static final String GROUP_KEY = "group";

    public static final String BACKUP_KEY = "backup";

    public static final String DEFAULT_KEY_PREFIX = "default.";

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    public static final String INTERFACE_KEY = "interface";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String ANYHOST_KEY = "anyhost";

    public static final String ASYNC_KEY = "async";

    public static final String SYNC_KEY = "sync";

    public static final String ONE_WAY_KEY= "oneway";

    public static final String RETURN_KEY = "return";

    public static final String $ECHO = "$echo";

    public static final String $HOT_DEPLOY = "$hot_deploy";

    public static final String TOKEN_KEY = "token";

    public static final String TIMEOUT_KEY = "timeout";

    public static final String PATH_KEY = "path";

    public static final String CACHE_KEY = "cache";

    public static final String COMMA_SEPARATOR = ",";

    public static final String TPS_LIMIT_RATE_KEY = "tps";

    public static final String TPS_LIMIT_INTERVAL_KEY = "tps.interval";

    public static final long DEFAULT_TPS_LIMIT_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    public static final String MONITOR_KEY = "monitor";

    public static final String APPLICATION_KEY = "application";

    public static final String INPUT_KEY = "input";

    public static final String OUTPUT_KEY = "output";

    public static final String SIDE_KEY = "side";

    public static final String PROVIDER_SIDE = "provider";

    public static final String CONSUMER_SIDE = "consumer";

    public static final String COUNT_PROTOCOL = "count";

    public static final String PROVIDER = "provider";

    public static final String CONSUMER = "consumer";

    public static final String $INVOKE = "$invoke";

    public static final String SEND_TYPE = "sendType";

}
