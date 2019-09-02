package com.chenws.iot.common.aspect;

import com.chenws.iot.common.annotaton.IgnorePrintLogAnnotation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Created by chenws on 2019/8/31.
 */
@Component
@Aspect
@Slf4j
public class ControllerLogAspect {
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isInfoEnabled()) {
            return joinPoint.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method m = methodSignature.getMethod();
        boolean annotationPresent = m.isAnnotationPresent(IgnorePrintLogAnnotation.class);
        if(annotationPresent)
            return joinPoint.proceed();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        Object[] args = joinPoint.getArgs();
        StringBuilder methodParams = new StringBuilder();
        if( args!= null){
            try{
                for(Object arg : args){
                    if(!(arg instanceof HttpServletRequest) && !(arg instanceof MultipartFile))
                        methodParams.append(objectMapper.writeValueAsString(arg));
                }
            }catch (Exception e){
                log.warn("uri为{}的请求无法转换请求参数。",uri);
            }
        }
        long start = System.currentTimeMillis();
        try {
            Object rspObj = joinPoint.proceed();
            String responseStr = "";
            if(rspObj != null){
                try{
                    responseStr = objectMapper.writeValueAsString(rspObj);
                }catch (Exception e){
                    log.warn("uri为{}的请求无法转换返回数据。",uri);
                }
            }
            log.info("SUCCESS--请求处理类：{}，处理方法：{}，请求uri：{}，请求方法类型：{}，请求参数：{}",joinPoint.getTarget().getClass().getName(),joinPoint.getSignature().getName(),uri,method, methodParams.toString());
            if ((StringUtils.isNotBlank(responseStr)) && (responseStr.length() > 512)) {
                log.info("SUCCESS--返回数据：{}", StringUtils.substring(responseStr, 0, 500));
            } else if(StringUtils.isNotBlank(responseStr)){
                log.info("SUCCESS--返回数据：{}", responseStr);
            }
            return rspObj;
        }
        catch (Throwable e) {
            log.info("FAIL--请求处理类：{}，处理方法：{}，请求uri：{}，请求方法类型：{}，请求参数：{}",joinPoint.getTarget().getClass().getName(),joinPoint.getSignature().getName(),uri,method, methodParams.toString());
            throw e;
        }finally {
            log.info("请求处理结束，消耗时间：{}ms",(System.currentTimeMillis() - start));
        }
    }

}
