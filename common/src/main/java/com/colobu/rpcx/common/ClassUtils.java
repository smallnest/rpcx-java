package com.colobu.rpcx.common;

import com.colobu.rpcx.rpc.ReflectUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author goodjava@qq.com
 */
public class ClassUtils {


    private ClassUtils() {

    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }


    public static Class getClassByName(String name) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    public static String getMethodKey(String className, String methodName, String[] parameterNames) {
        return className + "." + methodName + "(" + Arrays.stream(parameterNames).collect(Collectors.joining(",")) + ")";
    }

    public static String[] getMethodParameterNames(Method method) {
        Class<?>[] types = method.getParameterTypes();
        return Stream.of(types).map(it -> ReflectUtils.getName(it)).toArray(String[]::new);
    }


}
