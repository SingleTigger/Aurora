package com.chenws.iot.common.utils;

import com.chenws.iot.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenws on 2019/11/22.
 */
@Slf4j
public class RedisUtil {

    private final RedisTemplate<String,Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 为指定key设置过期时间
     * @param key 键
     * @param timeout 时间
     * @param unit 时间单位
     * @return 成功返回true
     */
    public Boolean expire(String key, long timeout,TimeUnit unit) {
        if (timeout > 0) {
            return redisTemplate.expire(key, timeout, unit);
        }
        return Boolean.FALSE;
    }

    /**
     * key 不存在的时候才set
     * @param key key
     * @param value value
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 设置成功返回true，否则false
     */
    public Boolean setIfAbsent(String key, Object value,long timeout,TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout,unit);
    }

    /**
     * key 不存在的时候才set
     * @param key key
     * @param value value
     * @return 插入成功返回true，否则false
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key,value);
    }

    /**
     * 返回为key的记录数
     * @param key key
     * @return 条数
     */
    public Long size(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    /**
     * 对指定key进行递减1
     * @param key 键
     * @return 如果不存在key，返回-1，否则返回减后值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 判断指定key是否存在
     * @param key 键
     * @return true：存在，false：不存在
     */
    public Boolean hasKey(String key) {
        if(StringUtils.isNotBlank(key)) {
            return redisTemplate.hasKey(key);
        }else {
            return Boolean.FALSE;
        }
    }

	/**
	 * 删除多个key
	 * @param keys key集合
	 */
	public void delete(Collection<String> keys){
        redisTemplate.delete(keys);
    }

    /**
     * 删除指定key
     * @param key key
     * @return true 删除成功
     */
    public Boolean delete(String key){
        return redisTemplate.delete(key);
    }

    /**
     * 获取指定key的值
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置key-value
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置指定key的值并设置其过期时间
     * @param key 键
     * @param value 值
     * @param timeout 时间：小于等于0将设置为无限期
     */
    public void set(String key, Object value, long timeout,TimeUnit unit) {
        if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            set(key, value);
        }
    }

    /**
     * 对指定key对应的值进行递增
     * @param key 键
     * @param delta 要增加的值(大于0)
     * @return Long 递增后的value值
     */
    public Long increment(String key, long delta) {
        if (delta < 0) {
            throw new CustomException("DELTA_ERROR");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 获取存储在哈希表中指定字段的值
     * @param key must not be {@literal null}.
     * @param hashKey must not be {@literal null}.
     * @return 值
     */
    public Object hGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取指定key对应的所有键值对
     * @param key 键
     * @return 对应的所有键值对
     */
    public Map<Object, Object> entries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 将Map存放到哈希表指定key中
     * @param key 键
     * @param map 对应多个键值
     */
    public void hmSet(String key, Map<Object, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("将Map存放到哈希表指定key中异常" + e);
        }
    }

    /**
     * 将哈希表指定key中的字段field值设为value，如果不存在则创建
     * @param key   键
     * @param hashKey 字段
     * @param value 值
     */
    public void hSet(String key, Object hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.error("将哈希表指定key中的字段field值设为value异常" + e);
        }
    }

    /**
     * 删除一个或多个哈希表字段
     * @param key   键，不能为null
     * @param hashKeys 字段：可以是一个或多个，不能为null
     */
    public void hDel(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 判断哈希表中是否存在指定key
     * @param key   键，不能为null
     * @param hashKey 字段，不能为null
     * @return true：存在，false：不存在
     */
    public Boolean hHasKey(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }


    /**
     * 根据key获取所有的值
     * @param key 键
     * @return 所有的值
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("根据key获取所有的值异常" + e);
            return null;
        }
    }

    /**
     * 从key中查询指定的value是否存在
     * @param key   键
     * @param value 值
     * @return true：存在，false：不存在
     */
    public Boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("从key中查询指定的value是否存在异常" + e);
            return Boolean.FALSE;
        }
    }

    /**
     * 向指定集合key中添加一或多个value
     * @param key    键
     * @param values 值，可以是一个或多个
     * @return 成功个数
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("向指定集合key中添加一或多个value异常" + e);
            return 0L;
        }
    }

    /**
     * 获取指定key的长度
     * @param key 键
     * @return 结果
     */
    public Long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取指定key的长度异常" + e);
            return 0L;
        }
    }

    /**
     * 移除指定key中的一个或多个value
     *
     * @param key    键
     * @param values 值，可以是一个或多个
     * @return 移除的个数
     */
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("移除指定key中的一个或多个value异常" + e);
            return 0L;
        }
    }

    /**
     * 获取列表key中指定范围内的元素
     * @param key   键
     * @param start 开始
     * @param end   结束
     * @return 结果
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取列表key中指定范围内的元素异常" + e);
            return null;
        }
    }

    /**
     * 获取指定列表key的长度
     * @param key 键
     * @return 长度
     */
    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取指定列表key的长度异常" + e);
            return 0L;
        }
    }

    /**
     * 通过索引获取指定key中的值
     * @param key   键
     * @param index 索引
     * @return 数据
     */
    public Object lIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("通过索引获取指定key中的值异常" + e);
            return null;
        }
    }

    /**
     * 给指定key设置值value
     * @param key   键
     * @param value 值
     */
    public void lRightPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("给指定key设置值value异常" + e);
        }
    }

    /**
     * 向指定key中添加一个List类型的值
     *
     * @param key 键
     * @param value 值
     */
    public void lRightPushAll(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
        } catch (Exception e) {
            log.error("向指定key中添加一个List类型的值异常" + e);
        }
    }

    /**
     * 通过索引修改指定key中的表元素的值
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lSaveByIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
        } catch (Exception e) {
            log.error("通过索引修改指定key中的表元素的值异常" + e);
        }
    }

    /**
     * 从指定key中移除N个列表元素
     * @param key 键
     * @param count 要移除的元素个数
     * @param value 值
     * @return 移除元素的个数
     */
    public Long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("从指定key中移除N个列表元素异常" + e);
            return 0L;
        }
    }

    /**
     * 获取所有键
     * @param pattern 正则
     * @return key集合
     */
    public Set<String> getKeys(String pattern){
        return redisTemplate.keys(pattern);
    }

    /**
     * 获取redis真正的key
     *
     * @param key     模式
     * @param objects 参数
     * @return String key
     */
    public String getKey(String key, Object... objects) {
        return MessageFormat.format(key, objects);
    }
}