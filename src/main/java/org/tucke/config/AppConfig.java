package org.tucke.config;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author tucke
 */
public class AppConfig {

    private static LinkedHashMap<String, Object> APPLICATION_CONFIG;

    private AppConfig() {
        throw new IllegalStateException();
    }

    public static void load(String[] env) {
        StringBuilder fileName = new StringBuilder("application");
        if (env != null) {
            for (String s : env) {
                String[] opt = s.split("=");
                if ("--env".equals(opt[0])) {
                    fileName.append("-").append(opt[1]);
                }
            }
        }
        Yaml yaml = new Yaml();
        InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(fileName.append(".yaml").toString());
        APPLICATION_CONFIG = yaml.load(inputStream);
    }

    /**
     * 获得指定KEY最近一层的数据
     *
     * @param key 指定的KEY
     * @param map 需要查找的MAP
     * @return 最近一层的数据
     */
    private static LinkedHashMap<?, ?> getNearestLevelMap(String key, LinkedHashMap<?, ?> map) {
        if (StringUtils.isNotBlank(key) && map != null) {
            String[] keys = key.split("\\.");
            if (keys.length > 1) {
                String key0 = keys[0];
                Object o = map.get(keys[0]);
                if (o instanceof LinkedHashMap<?, ?>) {
                    return getNearestLevelMap(key.substring(key0.length() + 1), (LinkedHashMap<?, ?>) o);
                } else {
                    return map;
                }
            }
        }
        return map;
    }

    /**
     * 按.分割字符串，并返回最后一层
     *
     * @param key 需要分割的KEY
     * @return 最后一层的KEY
     */
    private static String lastIndexKey(String key) {
        boolean flag = key.contains(".");
        if (flag) {
            return key.substring(key.lastIndexOf(".") + 1);
        }
        return key;
    }

    public static Object getObject(String key) {
        LinkedHashMap<?, ?> nearestLevelObject = getNearestLevelMap(key, APPLICATION_CONFIG);
        return nearestLevelObject.get(lastIndexKey(key));
    }

    public static String getString(String key) {
        return getObject(key).toString();
    }

    public static int getIntValue(String key) {
        String str = getString(key);
        if (str == null) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    public static boolean getBoolean(String key) {
        String str = getString(key);
        if (str == null) {
            return false;
        }
        return Boolean.parseBoolean(str);
    }

    public static double getDoubleValue(String key) {
        String str = getString(key);
        if (str == null) {
            return 0.0D;
        }
        return Double.parseDouble(str);
    }

    public static List<?> getList(String key) {
        Object obj = getObject(key);
        if (obj instanceof List<?>) {
            return (List<?>) obj;
        }
        return null;
    }

    public static LinkedHashMap<?, ?> getMap(String key) {
        Object obj = getObject(key);
        if (obj instanceof LinkedHashMap<?, ?>) {
            return (LinkedHashMap<?, ?>) obj;
        }
        return null;
    }

}
