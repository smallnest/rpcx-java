package com.colobu.rpcx.common;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author goodjava@qq.com
 */
public class ClassPathResource {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathResource.class);

    private final String path;

    private ClassLoader classLoader;


    public ClassPathResource(String path) {
        this.path = path;
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }


    public InputStream getInputStream() {
        InputStream is;
        if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            logger.warn("cannot be opened because it does not exist:{}", this.path);
        }
        return is;
    }


    public String getString() {
        try {
            InputStream is = getInputStream();
            if (null == is) {
                return "";
            }
            return new String(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Properties getProperties() {
        Properties prop = new Properties();
        try {
            InputStream is = getInputStream();
            if (null == is) {
                return prop;
            }
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

}
