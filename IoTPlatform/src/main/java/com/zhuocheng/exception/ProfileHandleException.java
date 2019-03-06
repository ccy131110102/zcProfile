package com.zhuocheng.exception;

/**
 * @Description: profile处理异常
 */
public class ProfileHandleException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 287535004419783345L;
	
	// 异常号
	private int errorCode;

	public ProfileHandleException(String msg,int errorCode) {
	        super(msg);
	        this.errorCode=errorCode;
	    }

	public int getErrorCode() {
		return errorCode;
	}

}
