package com.chenws.iot.common.enums;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by chenws on 2019/8/31.
 */
public interface BaseEnum {

    String getDesc();

    int getValue();

    static <T extends BaseEnum> T getEnum(Integer value, Class<T> enumClass) {
        if (value == null)
            return null;
        Supplier<Stream<T>> constantsStream = () -> Arrays.stream(enumClass.getEnumConstants());
        return constantsStream.get()
                .filter(enumConstant -> enumConstant.getValue() == value)
                .findAny()
                .orElse(constantsStream.get()
                        .filter(enumConstant -> enumConstant.getValue() == -1)
                        .findAny()
                        .orElse(null));
    }

    static <T extends BaseEnum> T getEnum(String desc, Class<T> enumClass) {
        Supplier<Stream<T>> constantsStream = () -> Arrays.stream(enumClass.getEnumConstants());

        return constantsStream.get()
                .filter(enumConstant -> enumConstant.getDesc().equals(desc))
                .findAny()
                .orElse(constantsStream.get()
                        .filter(enumConstant -> enumConstant.getValue() == -1)
                        .findAny()
                        .orElse(null));
    }

}
