package com.zhuocheng.processor.factory;

import java.text.ParseException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.DeviceInfoHandler;
import com.zhuocheng.handler.DeviceMessageHandler;
import com.zhuocheng.handler.ProfileHandler;
import com.zhuocheng.handler.ServiceMessageHandler;
import com.zhuocheng.processor.DecodeProcessor;
import com.zhuocheng.processor.EncodeProcessor;

import redis.clients.jedis.JedisPool;

/**
 * @Description: 处理器工厂类，用于按命令类型获取处理器
 */
public class ProcessorFactory {
	
	// 屏蔽构造函数
	private ProcessorFactory(){
		
	}
	
	/**
	 * @Description: 根据消息中的命令类型创建解码处理器
	 * @param message（完整消息体）,jedisPool
	 * @throws ProfileHandleException 
	 */
	public static DecodeProcessor createDecodeProcessor(String message, JedisPool jedisPool) throws ProfileHandleException{
		DeviceMessageHandler dmHandler = new DeviceMessageHandler(message);
		
		String messageId = "0x" + dmHandler.separateMessage().get(Constant.CONSTRUCTION_COMMAND);
		
		Map profile = ProfileHandler.getInstance().getDecodeProfileByMessageId(messageId);
		
		return new DecodeProcessor(profile, dmHandler.combileDecodeOriginalMessage());
	}
	
	/**
	 * @Description: 根据消息中的命令类型创建编码处理器
	 * @param message（完整消息体）,jedisPool
	 * @throws ProfileHandleException 
	 * @throws ParseException 
	 */
	public static EncodeProcessor createEncodeProcessor(String message, JedisPool jedisPool) throws ProfileHandleException, ParseException{
		Map serviceMessage = (Map)JSONObject.parse(message, Feature.OrderedField);
		
		ServiceMessageHandler dmHandler = new ServiceMessageHandler(serviceMessage);
		
		String deviceId = (String) serviceMessage.get(Constant.SERVICE_MESSAGE_DEVICEID);
		Map commandMap = (Map) serviceMessage.get(Constant.SERVICE_MESSAGE_COMMAND);
		
		String serviceId = (String) commandMap.get(Constant.SERVICE_MESSAGE_COMMAND_SERVICEID);
		String method = (String) commandMap.get(Constant.SERVICE_MESSAGE_COMMAND_METHOD);
		
		Map deviceInfoMap = DeviceInfoHandler.getInstance().getDeviceInfoByDeviceKey(deviceId);
		String appId = (String) deviceInfoMap.get(Constant.DEVICEINFO_APPID);
		String profileId = (String) deviceInfoMap.get(Constant.DEVICEINFO_PROFILEID);
		
		Map profile = ProfileHandler.getInstance().getEncodeProfileByMethod(serviceId, method, profileId, appId);
		
		return new EncodeProcessor(profile);
	}
}
