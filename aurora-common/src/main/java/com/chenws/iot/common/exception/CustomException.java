package com.chenws.iot.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by chenws on 2019/8/31.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String msg;

	private int code = 1;

	private String[] args;

	public CustomException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public CustomException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public CustomException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public CustomException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}
	public CustomException(int code, String msgFormat, Object... args) {
		super(String.format(msgFormat, args));
		this.code = code;
		this.msg = String.format(msgFormat, args);
	}

    public CustomException(int code, String msg, String[] args) {
        this.msg = msg;
        this.code = code;
        this.args = args;
    }

    public CustomException(int code, String msg, String arg) {
        this.msg = msg;
        this.code = code;
        this.args = new String[]{arg};
    }

}

