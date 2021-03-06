package blog.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * map缓存实现
 */
public class MapCache
{
    private static final int DEFAULT_CACHES = 1024;
    private static final MapCache INS = new MapCache();

    public static MapCache single()
    {
        return INS;
    }

    //    缓存容器
    private Map<String, CacheObject> cachePool;

    public MapCache()
    {
        this(DEFAULT_CACHES);
    }

    public MapCache(int cacheCount)
    {
        cachePool = new ConcurrentHashMap<>(cacheCount);
    }

    /**
     * 读取一个缓存
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get(String key)
    {
        CacheObject cacheObject = cachePool.get(key);
        if (cacheObject != null)
        {
            long cur = System.currentTimeMillis() / 1000;
            if (cacheObject.getExpired() <= 0 || cacheObject.getExpired() > cur)
            {
                Object result = cacheObject.getValue();
                return (T) result;
            }
        }
        return null;
    }

    /**
     * 读取一个hash类型的缓存
     *
     * @param key
     * @param field
     * @param <T>
     * @return
     */
    public <T> T hget(String key, String field)
    {
        key = key + ":" + field;
        return this.get(key);
    }

    /**
     * 设置一个缓存
     *
     * @param key
     * @param value
     * @param expired
     */
    public void set(String key, Object value, long expired)
    {
        expired = expired > 0 ? System.currentTimeMillis() / 1000 + expired : expired;
        CacheObject cacheObject = new CacheObject(key, value, expired);
        cachePool.put(key, cacheObject);
    }

    public void set(String key, Object value)
    {
        this.set(key, value, -1);
    }

    /**
     * 设置一个hash缓存
     *
     * @param key
     * @param field
     * @param value
     * @param expired
     */
    public void hset(String key, String field, Object value, long expired)
    {
        key = key + ":" + field;
        expired = expired > 0 ? System.currentTimeMillis() / 1000 + expired : expired;
        CacheObject cacheObject = new CacheObject(key, value, expired);
        cachePool.put(key, cacheObject);
    }

    public void hset(String key, String field, Object value)
    {
        this.hset(key, field, value, -1);
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void del(String key)
    {
        cachePool.remove(key);
    }

    public void hdel(String key, String field)
    {
        key = key + ":" + field;
        this.del(key);
    }

    //清空缓存
    public void clean()
    {
        cachePool.clear();
    }

    static class CacheObject
    {
        private String key;
        private Object value;
        private long expired;

        public CacheObject(String key, Object value, long expired)
        {
            this.key = key;
            this.value = value;
            this.expired = expired;
        }

        public String getKey()
        {
            return key;
        }

        public Object getValue()
        {
            return value;
        }

        public long getExpired()
        {
            return expired;
        }
    }

}
