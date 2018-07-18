package com.colobu.rpcx.common;

import com.google.common.io.ByteStreams;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClassPathResource {

    private final String path;

    private ClassLoader classLoader;


    public ClassPathResource(String path) {
        this.path = path;
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }


    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException("cannot be opened because it does not exist");
        }
        return is;
    }


    public String getString() {
        try {
            InputStream is = getInputStream();
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
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

}
