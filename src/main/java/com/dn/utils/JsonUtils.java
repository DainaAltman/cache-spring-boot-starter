package com.dn.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JsonUtils {

    private static final Gson GSON = new Gson();

    /**
     * 将字符串转换为 json 化后的 Object 对象
     *
     * @param json
     * @return
     */
    private static JsonObject jsonObject(String json) {
        try {
            return new JsonParser().parse(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            log.error("无法解析 json 字符串", e);
        }
        return null;
    }

    /**
     * 将字符串转换为 json 化后的 array 对象
     *
     * @param json
     * @return
     */
    private static JsonArray jsonArray(String json) {
        return new JsonParser().parse(json).getAsJsonArray();
    }

    /**
     * 将字符串转换为 List
     *
     * @param json  json字符串
     * @param clazz 转换的类的类型
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        return Arrays.asList(GSON.fromJson(json, TypeToken.getArray(clazz).getType()));
    }

    /**
     * 将一个 json 字符串转换为 List
     *
     * @param json json字符串
     * @return
     */
    public static List<Object> toList(String json) {
        return toList(jsonArray(json));
    }

    /**
     * 将一个 json 字符串转换为 Set
     *
     * @param json json字符串
     * @return
     */
    public static Set<Object> toSet(String json) {
        return toList(json).stream().collect(Collectors.toSet());
    }

    /**
     * 将一个 json 字符串转换为 Set, 并且指定 Set 元素中的 Bean 类型
     * @param json  json字符串
     * @param clazz Bean类型
     * @param <T>
     * @return
     */
    public static <T> Set<T> toSet(String json, Class<T> clazz) {
        return toList(json, clazz).stream().collect(Collectors.toSet());
    }

    /**
     * 底层的将一个 array 化的 json 对象转换为 List 对象的方法
     *
     * @param jsonArray
     * @return
     */
    private static List<Object> toList(JsonArray jsonArray) {
        List<Object> list = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            Object val = jsonArray.get(i);
            if (val instanceof JsonArray) {
                list.add(toList((JsonArray) val));
            } else if (val instanceof JsonObject) {
                list.add(toMap((JsonObject) val));
            } else {
                list.add(val);
            }
        }
        return list;
    }

    /**
     * 将一个 json 对象转换为 Map[String,Object] 对象的方法
     *
     * @param json
     * @return
     */
    public static Map<String, Object> toMap(String json) {
        return toMap(jsonObject(json));
    }

    /**
     * 将一个 json 对象转换为 Map[Key,Val] 对象的方法
     *
     * @param json
     * @param kClass
     * @param vClass
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        return toMap(jsonObject(json), kClass, vClass);
    }

    public static <K, V> Map<K, V> toMap(JsonObject jsonObject, Class<K> keyClass, Class<V> valClass) {
        Map<K, V> map = new HashMap();
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            K k = JsonUtils.toBean(key, keyClass);

            Object value = entry.getValue();
            V v = JsonUtils.toBean(JsonUtils.toJson(value), valClass);
            map.put(k, v);
        }
        return map;
    }

    /**
     * 底层的将一个 json 对象转换为 map 对象的方法
     *
     * @param jsonObject
     * @return
     */
    private static Map<String, Object> toMap(JsonObject jsonObject) {
        Map<String, Object> map = new HashMap();
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof JsonArray) {
                map.put(key, toList((JsonArray) value));
            } else if (value instanceof JsonObject) {
                map.put(key, toMap((JsonObject) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将字符串转换为 JavaBean
     *
     * @param json  json字符串
     * @param clazz 转换的类的类型
     * @param <T>
     * @return
     */
    public static <T> T toBean(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * 将 JavaBean 转换为字符串
     *
     * @param bean 转换的类对象
     * @param <T>
     * @return
     */
    public static <T> String toJson(T bean) {
        return GSON.toJson(bean, bean.getClass());
    }
}
