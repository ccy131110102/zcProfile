package com.zhuocheng.exception;

public class ProcessorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4391343318664845611L;
	// 异常号
	private int errorCode;

	public ProcessorException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
