package com.zhuocheng.processor.Interface;

import java.util.Map;

import com.zhuocheng.exception.ProcessorException;

/**
 * @Description: 处理器接口，定义命令解码处理器结构
 */
public interface IDecodeProcessor {
	
	/**
	 * @Description: 根据规则解析数据域并生成键值对返回
	 * @param
	 * @throws ProcessorException 
	 * @throws Exception 
	 */
	public Map decode() throws ProcessorException, Exception;
	
	/**
	 * @Description: 根据规则分割信息
	 * @param message（完整的信息体）
	 */
//	public String[] separate(String messasge);
}
