package com.zhuocheng.exception;

public class HttpRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3151362656832353263L;
	// 异常号
	private int errorCode;

	public HttpRequestException(String msg, int errorCode) {
			super(msg);
			this.errorCode = errorCode;
		}

	public int getErrorCode() {
		return errorCode;
	}
}
