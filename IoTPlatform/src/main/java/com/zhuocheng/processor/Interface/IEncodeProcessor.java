package com.zhuocheng.processor.Interface;

import java.text.ParseException;
import java.util.Map;

import com.zhuocheng.exception.ProfileHandleException;

/**
 * @Description: 处理器接口，定义命令编码处理器结构
 */
public interface IEncodeProcessor {
	/**
	 * @Description: 根据规则将结构体编码并拼接
	 * @param rspContruction（待编码的信息结构体）
	 * @throws ProfileHandleException 
	 * @throws ParseException 
	 */
	public Map combile(Map rspContruction) throws ProfileHandleException, ParseException;
	
}
