package com.example.trade;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisUtil {
    private static final JedisPool jedisPool;

    public static void main(String[] args) {
        List<SealStock> sealStocks = SealStockCrawler.loadSealStock(null, null);
        RedisUtil.deleteKey("sealStocks");
        RedisUtil.setList("sealStocks", sealStocks);
        List<SealStock> sealStocks1 = RedisUtil.getList("sealStocks", SealStock.class);
        BigDecimal latestPrice = sealStocks1.get(0).getLatestPrice();
        System.out.println(latestPrice);
    }

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10); // 最大连接数
        poolConfig.setMaxIdle(5);   // 最大空闲连接数
        poolConfig.setMinIdle(2);   // 最小空闲连接数
        jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }

    private static Jedis getJedis() {
        return jedisPool.getResource();
    }

    // 序列化对象为JSON字符串
    private static String serializeObject(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    // 反序列化JSON字符串为对象
    private static <T> T deserializeObject(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }

    // 序列化对象为JSON字符串
    private static <T> String serializeList(List<T> obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    // 反序列化JSON字符串为对象
    private static <T> List<T> deserializeList(String json, Class<T> clazz) {
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }

    // 设置单个对象到Redis
    public static <T> void setObject(String key, T value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, serializeObject(value));
        }
    }

    // 从Redis获取单个对象
    public static <T> T getObject(String key, Class<T> clazz) {
        try (Jedis jedis = getJedis()) {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }
            return deserializeObject(json, clazz);
        }
    }

    // 设置Map结构到Redis
    public static <K, V> void setMap(String mapKey, Map<K, V> map) {
        try (Jedis jedis = getJedis()) {
            if (mapKey == null || map.isEmpty()) {
                return;
            }
            Map<String, String> serializedMap = new HashMap<>();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                serializedMap.put(serializeObject(entry.getKey()), serializeObject(entry.getValue()));
            }
            jedis.hmset(mapKey, serializedMap);
        }
    }


    // 从Redis获取Map结构
    public static <V> Map<String, V> getMap(String mapKey, Class<V> clazz) {
        try (Jedis jedis = getJedis()) {
            Map<String, String> serializedMap = jedis.hgetAll(mapKey);
            if (serializedMap == null) {
                return new HashMap<>();
            }
            Map<String, V> deserializedMap = new HashMap<>();
            for (Map.Entry<String, String> entry : serializedMap.entrySet()) {
                deserializedMap.put(entry.getKey(), deserializeObject(entry.getValue(), clazz));
            }
            return deserializedMap;
        }
    }

    // redis设置list
    public static <T> void setList(String key, List<T> list) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, serializeList(list));
        }
    }

    // redis获取list
    public static <T> List<T> getList(String key, Class<T> clazz) {
        try (Jedis jedis = getJedis()) {
            String json = jedis.get(key);
            if (json == null) {
                return new ArrayList<>();
            }
            return deserializeList(json, clazz);
        }
    }

    // redis 设置map，key为字符串，value为List<实体>
    public static <T> void setMapList(String mapKey, Map<String, List<T>> map) {
        try (Jedis jedis = getJedis()) {
            if (mapKey == null || map.isEmpty()) {
                return;
            }
            Map<String, String> serializedMap = new HashMap<>();
            for (Map.Entry<String, List<T>> entry : map.entrySet()) {
                serializedMap.put(entry.getKey(), serializeList(entry.getValue()));
            }
            jedis.hmset(mapKey, serializedMap);
        }
    }

    public static <T> void setMapList(String mapKey, String fieldKey, List<T> list) {
        try (Jedis jedis = getJedis()) {
            if (list == null || list.isEmpty()) {
                return;
            }
            jedis.hset(mapKey, fieldKey, serializeList(list));
        }
    }

    // redis 获取map，key为字符串，value为List<实体>
    public static <T> List<T> getMapList(String mapKey, String fieldKey, Class<T> clazz) {
        try (Jedis jedis = getJedis()) {
            String value = jedis.hget(mapKey, fieldKey);
            if (value == null) {
                return new ArrayList<>();
            }
            return deserializeList(value, clazz);
        }
    }

    // redis 获取map，key为字符串，value为List<实体>
    public static <T> Map<String, List<T>> getMapList(String mapKey, Class<T> clazz) {
        try (Jedis jedis = getJedis()) {
            Map<String, String> serializedMap = jedis.hgetAll(mapKey);
            if (serializedMap == null) {
                return new HashMap<>();
            }
            Map<String, List<T>> deserializedMap = new HashMap<>();
            for (Map.Entry<String, String> entry : serializedMap.entrySet()) {
                deserializedMap.put(entry.getKey(), deserializeList(entry.getValue(), clazz));
            }
            return deserializedMap;
        }
    }

    // 删除键
    public static void deleteKey(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(key);
        }
    }

    // 关闭连接池
    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
