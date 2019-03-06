package com.zhuocheng.processor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.ProcessorException;
import com.zhuocheng.processor.Interface.IDecodeProcessor;
import com.zhuocheng.util.ParamConverter;

/**
 * @Description: 解码处理器
 */
public class DecodeProcessor extends Processor implements IDecodeProcessor{
	
	private Map originalMessage;
	
	public DecodeProcessor(Map profile, Map originalMessage) {
		super(profile);
		this.originalMessage = originalMessage;
	}

	/**
	 * @Description: 根据规则解析数据域并生成键值对返回
	 * @param 
	 * @throws ProcessorException 
	 */
	public Map decode() throws ProcessorException, Exception {
		String profileId = "";
		String serverId = "";
		String serivceType = "";
		String appId = "";
		String commandType = "";
		
		if(originalMessage == null){
			throw new ProcessorException(Constant.CONSTRUCTION_OP_ERROR_DECODENOPROFILE_MSG, Constant.CONSTRUCTION_OP_ERROR_DECODENOPROFILE_CODE);
		}
		
		// 1.获取profile中的参数列表
		Iterator appIt = profile.keySet().iterator();
		appId = (String) appIt.next();
		Map appProfileMap = (Map) profile.get(appId);
		Iterator profileIt = appProfileMap.keySet().iterator();
		profileId = (String) profileIt.next();
		
		
		Map profileMap = (Map) appProfileMap.get(profileId);
		Iterator serverIt = profileMap.keySet().iterator();
		serverId = (String) serverIt.next();
		Map methodsMap = (Map) profileMap.get(serverId);
		Iterator methodIt = methodsMap.keySet().iterator();
		serivceType = (String) methodIt.next();
		Map methodMap = (Map) methodsMap.get(serivceType);
		JSONArray paraList = (JSONArray) methodMap.get(Constant.PROPERTIES_PARASKEY);
		commandType = (String) methodMap.get(Constant.PROPERTIES_COMMAND);;
		
		// 2.根据参数列表解析数据域
		// 2.1获取数据域
		String data = (String) originalMessage.get("data");
		String deviceId = (String) originalMessage.get("deviceId");
		// 2.2遍历参数列表并截取数据域保存至map
		Map dataMap = new LinkedHashMap();
		Map serviceMap = new LinkedHashMap();
		
		for(Object para : paraList){
			String paraName =  (String) ((Map)para).get(Constant.PROPERTIES_PROPERTYNAME);
			String paraType = (String) ((Map)para).get(Constant.PROPERTIES_DATATYPE);
			int paraLength = (int) ((Map)para).get(Constant.PROPERTIES_LENGTH) * 2;
			
			String paraData = data.substring(0, paraLength);
			System.out.println("数据内容：" + paraData + "---" + "参数名：" + paraName + "---" + "参数类型：" + paraType + "---" + "参数长度：" + paraLength);

			paraData = String.valueOf(ParamConverter.dataToParam(paraType, paraLength, paraData));
			
			data = data.substring(paraLength, data.length());
			
			dataMap.put(paraName, paraData);
		}
		
		// 2.3拼接完整解码Map
		originalMessage.remove(Constant.CONSTRUCTION_DATAKEY);
		originalMessage.remove(Constant.CONSTRUCTION_MESSAGEIDKEY);
		serviceMap.put(Constant.PROFILE_ID, profileId);
		serviceMap.put(Constant.SERVICE_ID, serverId);
		serviceMap.put(Constant.SERVICE_TYPE, serivceType);
		serviceMap.put(Constant.SERVICE_DATA, dataMap);
		serviceMap.put(Constant.SERVICE_COMMAND_TYPR, commandType);
		originalMessage.put(Constant.CONSTRUCTION_SERVICEKEY, serviceMap);
		
		return originalMessage;
	}

}
