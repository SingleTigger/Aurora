package com.chenws.iot.common.exception;

import com.chenws.iot.common.utils.CommonUtils;
import com.chenws.iot.common.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionAdvice {
    @ExceptionHandler(CustomException.class)
    public ResponseVO<?> exceptionHandler(HttpServletRequest request, CustomException e) {

        String[] args = e.getArgs();
        String msg = e.getMsg();
        if (null != args && args.length > 0) {
            msg = Normalizer.normalize(msg, Normalizer.Form.NFKC);
            Pattern pattern = Pattern.compile("\\{(\\d)}");
            Matcher m = pattern.matcher(msg);
            while (m.find()) {
                String group = m.group(1);
                try {
                    if (null != group && Integer.parseInt(group) >= 0 && Integer.parseInt(group) < args.length) {
                        msg = msg.replace(m.group(), args[Integer.parseInt(group)]);
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }
        //设置自定义异常的消息
        e.setMsg(msg);
        log.warn("[请求URI]->" + request.getRequestURI() + "[运行时发生自定义已知异常]->" + msg);
        return CommonUtils.errorResponseVO(e);
    }

    /*在DTO中使用javax.validation.constraints包下的注解校验参数时，
     若参数校验失败，将失败信息返回给前端*/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO<?> exceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        String message = String.join(",", errorMessages);
        return exceptionHandler(request, new CustomException(message, e));
    }
}
