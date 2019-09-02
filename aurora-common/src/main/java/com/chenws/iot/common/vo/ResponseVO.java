package com.chenws.iot.common.vo;

import com.chenws.iot.common.enums.ResultStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenws on 2019/8/31.
 */
@Data
public class ResponseVO<T> implements Serializable {

	private static final long serialVersionUID = 6775422262797117144L;

	private Integer value;

	private String desc;

	private T data;

	public ResponseVO(){
		this.value = ResultStateEnum.SUCCESS.getValue();
		this.desc = ResultStateEnum.SUCCESS.getDesc();
	}

	public ResponseVO(T data){
		this.value = ResultStateEnum.SUCCESS.getValue();
		this.desc = ResultStateEnum.SUCCESS.getDesc();
		this.data = data;
	}
	
	public ResponseVO(ResultStateEnum result) {
		this.value = result.getValue();
		this.desc = result.getDesc();
	}
	
	public ResponseVO(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public ResponseVO(int value, String desc, T data) {
		this.value = value;
		this.desc = desc;
		this.data = data;
	}

	public ResponseVO(Throwable e) {
		super();
		this.desc = e.getMessage();
		this.value = ResultStateEnum.FAIL.getValue();
	}

}
