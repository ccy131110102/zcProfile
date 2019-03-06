package com.zhuocheng.processor;

import java.util.Map;

import org.apache.log4j.Logger;

import com.zhuocheng.constant.Constant;
import com.zhuocheng.controller.ServiceController;
import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.subscribe.SubscribePublish;

/**
 * @Description: 处理器超类
 */
public class Processor {
	// 用户保存指定命令的profile
	protected Map profile;
	
	private Logger logger = Logger.getLogger(Processor.class);

	/**
	 * @Description: 构造函数，用于初始化profile
	 * @param message（完整的信息体）
	 */
	public Processor(Map profile) {
		this.profile = profile;
	}

	/**
	 * @Description: 发布消息
	 * @param message（完整的信息体）
	 * @throws HttpRequestException
	 */
	public String publish(Map message, String type) throws HttpRequestException {
		String serviceId = "";
		String serviceType = "";
		String profileId = "";
		String saveId = "";
		String commandType = "";
		
		if (!type.equals(Constant.PUBLISH_TYPE_METHOD)) {
			serviceId = (String) ((Map) message.get(Constant.CONSTRUCTION_SERVICEKEY)).get(Constant.SERVICE_ID);
			serviceType = (String) ((Map) message.get(Constant.CONSTRUCTION_SERVICEKEY)).get(Constant.SERVICE_TYPE);
			profileId = (String) ((Map) message.get(Constant.CONSTRUCTION_SERVICEKEY)).get(Constant.PROFILE_ID);
			commandType = (String) ((Map) message.get(Constant.CONSTRUCTION_SERVICEKEY)).get(Constant.SERVICE_COMMAND_TYPR);
			
		} else {
			serviceId = (String) ((Map) message).get(Constant.SERVICE_ID);
			serviceType = (String) ((Map) message).get("method");
			profileId = (String) ((Map) message).get(Constant.PROFILE_ID);
			commandType = "0x" + (String) ((Map) message).get("command");
		}
		
		try {
			saveId = SubscribePublish.getInstance().publish(profileId, message, true, serviceType, serviceId, type, serviceId, commandType);
		} catch (ProfileHandleException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
//			e.printStackTrace();
		}

		return saveId;
	}
}
