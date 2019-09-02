package com.chenws.iot.common.utils;

import com.chenws.iot.common.exception.CustomException;
import com.chenws.iot.common.vo.ResponseVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
public abstract class CommonUtils {


    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final Integer SUCCESS_CODE = 0;

    private static final String SUCCESS_MSG = "success";

    private static final Integer UNKNOWN_EXCEPTION_CODE = -1;


    static {
        JSON_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JSON_MAPPER.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
    }

    public static String toJson(Object bean) {
        if (bean == null) {
            return null;
        }
        try {
            return JSON_MAPPER.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.warn("JSON转换异常", e);
            return null;
        }
    }

    public static Map<String, Object> getParamMap(Object obj){
        if (obj == null) {
            return null;
        }
        boolean ret = true;
        Class oo = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();
        do {
            clazzs.add(oo);
            oo = oo.getSuperclass();
        } while (oo != null && oo != Object.class);

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            for (Class clazz : clazzs) {
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                        continue;
                    }
                    field.setAccessible(true);
                    map.put(field.getName(), field.get(obj));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> ResponseVO<T> okResponseVO(T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setValue(SUCCESS_CODE);
        vo.setDesc(SUCCESS_MSG);
        vo.setData(t);
        return vo;
    }


    public static ResponseVO errorResponseVO(Exception e) {
        ResponseVO<String> vo = new ResponseVO<>();
        if (e instanceof CustomException) {
            CustomException ce = (CustomException) e;
            vo.setValue(ce.getCode());
            vo.setDesc(ce.getMsg());
        } else {
            vo.setValue(UNKNOWN_EXCEPTION_CODE);
            vo.setDesc(e.getMessage());
            vo.setData(stackTrace2String(e));
        }
        return vo;
    }

    public static String stackTrace2String(Throwable e) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        e.printStackTrace(ps);
        return os.toString();
    }


    public static ResponseVO<?> errorResponseVO(String msg) {
        ResponseVO<?> responseVO = new ResponseVO<>();
        responseVO.setValue(UNKNOWN_EXCEPTION_CODE);
        responseVO.setDesc(msg);
        return responseVO;
    }

    public static void copyProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target);
    }

    public static String javaFieldName2DatabaseFieldName(String javaFieldName) {
        return tk.mybatis.mapper.util.StringUtil.camelhumpToUnderline(javaFieldName);
    }


}
