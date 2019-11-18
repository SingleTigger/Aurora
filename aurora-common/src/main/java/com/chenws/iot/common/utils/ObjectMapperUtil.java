package com.chenws.iot.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by chenws on 2019/11/18.
 */
@Slf4j
public final class ObjectMapperUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper
                // 是否需要排序
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                // 忽略空bean转json的错误
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 取消默认转换timestamps形式
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                // 序列化的时候，过滤null属性
                .setSerializationInclusion(Include.NON_NULL)
                // 忽略在json字符串中存在，但在java对象中不存在对应属性的情况，防止错误
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 忽略空bean转json的错误
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private ObjectMapperUtil(){

    }

    /**
     * 对象转json字符串
     *
     * @param obj 对象
     * @param <T> 对象泛型
     * @return json字符串
     */
    public static <T> String objToJson(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("对象解析为json字符串异常", e);
        }
        return null;
    }

    /**
     * 对象转bytes
     *
     * @param obj 对象
     * @param <T> 对象泛型
     * @return json字符串
     */
    public static <T> byte[] objToBytes(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            log.warn("对象解析为byte数组异常", e);
        }
        return null;
    }

    /**
     * 对象转json字符串，并进行格式化
     *
     * @param obj 对象
     * @param <T> 对象泛型
     * @return 格式化后的json字符串
     */
    public static <T> String obj2JsonPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("对象解析为json字符串异常", e);
        }
        return null;
    }

    /**
     * json转对象
     * 这里有一个坑，例如List<User> list = stringObj(str,Class<List> data);这里会有问题
     *
     * @param jsonStr json字符串
     * @param clazz   对象
     * @param <T>     对象泛型
     * @return 反序列化后的对象
     */
    public static <T> T str2Obj(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr) || null == clazz) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) jsonStr : objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.warn("json字符串解析为对象异常", e);
            return null;
        }
    }

    /**
     * 复杂对象反序列化
     * 使用例子List<User> list = JsonUtil.string2Obj(str, new TypeReference<List<User>>() {});
     *
     * @param str           json对象
     * @param typeReference 引用类型
     * @param <T>           返回值类型
     * @return 反序列化对象
     */
    public static <T> T str2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            log.warn("json字符串解析为对象错误", e);
            return null;
        }
    }

    /**
     * 复杂对象反序列化
     * 使用例子List<User> list = JsonUtil.string2Obj(str, List.class, User.class);
     *
     * @param str             json对象
     * @param collectionClass 定义的class类型
     * @param elementClass    子元素的class类型
     * @param <T>             返回值类型
     * @return 反序列化对象
     */
    public static <T> T str2Obj(String str, Class<?> collectionClass, Class<?>... elementClass) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            log.warn("json字符串解析为对象错误", e);
            return null;
        }
    }

    /**
     * 对象转换 && 对象深复制
     *
     * @param obj   源对象
     * @param clazz 目标对象引用类型
     * @param <T>   返回值类型
     * @return T
     */
    public static <T> T convert(Object obj, TypeReference<T> clazz) {
        try {
            return objectMapper.convertValue(obj, clazz);
        } catch (Exception e) {
            log.warn("对象转换异常", e);
        }
        return null;
    }

    /**
     * 对象转换 && 对象深复制
     *
     * @param obj   源对象
     * @param clazz 目标对象类型
     * @param <T>   返回值类型
     * @return T
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        try {
            return objectMapper.convertValue(obj, clazz);
        } catch (Exception e) {
            log.warn("对象转换异常", e);
        }
        return null;
    }

}

