package com.colobu.rpcx.rpc;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReflectUtils {

    private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();


    /**
     * void(V).
     */
    public static final char JVM_VOID = 'V';

    /**
     * boolean(Z).
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * byte(B).
     */
    public static final char JVM_BYTE = 'B';

    /**
     * char(C).
     */
    public static final char JVM_CHAR = 'C';

    /**
     * double(D).
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * float(F).
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * int(I).
     */
    public static final char JVM_INT = 'I';

    /**
     * long(J).
     */
    public static final char JVM_LONG = 'J';

    /**
     * short(S).
     */
    public static final char JVM_SHORT = 'S';


    public static String getDesc(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }

        if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t)) ret.append(JVM_VOID);
            else if ("boolean".equals(t)) ret.append(JVM_BOOLEAN);
            else if ("byte".equals(t)) ret.append(JVM_BYTE);
            else if ("char".equals(t)) ret.append(JVM_CHAR);
            else if ("double".equals(t)) ret.append(JVM_DOUBLE);
            else if ("float".equals(t)) ret.append(JVM_FLOAT);
            else if ("int".equals(t)) ret.append(JVM_INT);
            else if ("long".equals(t)) ret.append(JVM_LONG);
            else if ("short".equals(t)) ret.append(JVM_SHORT);
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }


    public static String desc2name(String desc) {
        StringBuilder sb = new StringBuilder();
        int c = desc.lastIndexOf('[') + 1;
        if (desc.length() == c + 1) {
            switch (desc.charAt(c)) {
                case JVM_VOID: {
                    sb.append("void");
                    break;
                }
                case JVM_BOOLEAN: {
                    sb.append("boolean");
                    break;
                }
                case JVM_BYTE: {
                    sb.append("byte");
                    break;
                }
                case JVM_CHAR: {
                    sb.append("char");
                    break;
                }
                case JVM_DOUBLE: {
                    sb.append("double");
                    break;
                }
                case JVM_FLOAT: {
                    sb.append("float");
                    break;
                }
                case JVM_INT: {
                    sb.append("int");
                    break;
                }
                case JVM_LONG: {
                    sb.append("long");
                    break;
                }
                case JVM_SHORT: {
                    sb.append("short");
                    break;
                }
                default:
                    throw new RuntimeException();
            }
        } else {
            sb.append(desc.substring(c + 1, desc.length() - 1).replace('/', '.'));
        }
        while (c-- > 0) sb.append("[]");
        return sb.toString();
    }


    public static Class<?> forName(String name) {
        try {
            return name2class(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not found class " + name + ", cause: " + e.getMessage(), e);
        }
    }


    public static Class<?> name2class(String name) throws ClassNotFoundException {
        return name2class(Thread.currentThread().getClass().getClassLoader(), name);
    }

    private static Class<?> name2class(ClassLoader cl, String name) throws ClassNotFoundException {
        int c = 0, index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        if (c > 0) {
            StringBuilder sb = new StringBuilder();
            while (c-- > 0)
                sb.append("[");

            if ("void".equals(name)) sb.append(JVM_VOID);
            else if ("boolean".equals(name)) sb.append(JVM_BOOLEAN);
            else if ("byte".equals(name)) sb.append(JVM_BYTE);
            else if ("char".equals(name)) sb.append(JVM_CHAR);
            else if ("double".equals(name)) sb.append(JVM_DOUBLE);
            else if ("float".equals(name)) sb.append(JVM_FLOAT);
            else if ("int".equals(name)) sb.append(JVM_INT);
            else if ("long".equals(name)) sb.append(JVM_LONG);
            else if ("short".equals(name)) sb.append(JVM_SHORT);
            else sb.append('L').append(name).append(';'); // "java.lang.Object" ==> "Ljava.lang.Object;"
            name = sb.toString();
        } else {
            if ("void".equals(name)) return void.class;
            else if ("boolean".equals(name)) return boolean.class;
            else if ("byte".equals(name)) return byte.class;
            else if ("char".equals(name)) return char.class;
            else if ("double".equals(name)) return double.class;
            else if ("float".equals(name)) return float.class;
            else if ("int".equals(name)) return int.class;
            else if ("long".equals(name)) return long.class;
            else if ("short".equals(name)) return short.class;
        }

        if (cl == null)
            cl = ClassHelper.getClassLoader();
        Class<?> clazz = NAME_CLASS_CACHE.get(name);
        if (clazz == null) {
            clazz = Class.forName(name, true, cl);
            NAME_CLASS_CACHE.put(name, clazz);
        }
        return clazz;
    }


    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }


    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }


}
