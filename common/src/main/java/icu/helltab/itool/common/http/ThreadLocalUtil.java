package icu.helltab.itool.common.http;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {
    private static final ThreadLocal<Object> TL = new ThreadLocal();

    private ThreadLocalUtil() {
    }

    public static <T> T get(T t) {
        Map<Class<?>, T> map = getMap();
        T res = map.get(t.getClass());
        if (null == res) {
            res = t;
            map.put(t.getClass(), t);
        }

        return res;
    }

    private static <T> Map<Class<?>, T> getMap() {
        Map<Class<?>, T> map = (Map)TL.get();
        if (map == null) {
            map = new HashMap();
            TL.set(map);
        }

        return (Map)map;
    }

    public static <T> void set(T t) {
        Map<Class<?>, T> map = getMap();
        map.put(t.getClass(), t);
    }

    public static void remove() {
        TL.remove();
    }
}
