package com.chenws.iot.common.annotaton;

import java.lang.annotation.*;

/**
 * Created by chenws on 2019/8/31.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface IgnorePrintLogAnnotation {

}
