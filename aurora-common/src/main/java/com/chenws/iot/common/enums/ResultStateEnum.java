package com.chenws.iot.common.enums;

import lombok.Getter;

/**
 * Created by chenws on 2019/8/31.
 */
public enum ResultStateEnum implements BaseEnum{
	
	SUCCESS("success",0),
	FAIL("fail",1),
    ;

	@Getter
	private String desc;

	@Getter
	private int value;
	
	ResultStateEnum(String desc, int value) {
		this.desc = desc;
		this.value = value;
	}


}
