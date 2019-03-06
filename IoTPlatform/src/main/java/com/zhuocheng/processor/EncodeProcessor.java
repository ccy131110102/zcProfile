package com.zhuocheng.processor;

import java.text.ParseException;
import java.util.Map;

import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.ServiceMessageHandler;
import com.zhuocheng.processor.Interface.IEncodeProcessor;

/**
 * @Description: 编码处理器
 */
public class EncodeProcessor extends Processor implements IEncodeProcessor {
	

	public EncodeProcessor(Map profile) {
		super(profile);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @Description: 根据规则将结构体编码并拼接
	 * @param rspContruction（待编码的信息结构体）
	 * @throws ProfileHandleException 
	 * @throws ParseException 
	 */
	public Map combile(Map rspContruction) throws ProfileHandleException, ParseException {

		return new ServiceMessageHandler(rspContruction).combileMessageMap();
	}

}
