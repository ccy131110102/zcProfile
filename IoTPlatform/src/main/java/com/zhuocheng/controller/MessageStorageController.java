package com.zhuocheng.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.handler.MessageStorageHandler;
import com.zhuocheng.handler.ServiceMessageHandler;

/**
 * @Description: 信息存储控制器
 */
@Controller
public class MessageStorageController {

	/**
	 * @Description: 存储信息
	 */
	@RequestMapping(value = "/message/store", method = RequestMethod.GET)
	@ResponseBody
	public String storeMessage(@RequestParam(value = "type") String type, @RequestParam(value = "message") String message,
			@RequestParam(value = "deviceId") String deviceId, @RequestParam(value = "appId") String appId) {
//		return MessageStorageHandler.getInstance().saveMessage(
//				ServiceMessageHandler.messageMapToMessage((Map) JSONObject.parse(message)), appId, profileId, deviceId,
//				Constant.COMMAND_SAVETYPE_DOWN, "0");
		
		return "";
	}
	
	/**
	 * @return 
	 * @Description: 取暂存
	 */
	@RequestMapping(value = "/message/retire", method = RequestMethod.GET)
	@ResponseBody
	public String retireMessage(@RequestParam(value = "appId")String appId, @RequestParam(value = "deviceId")String deviceId, @RequestParam(value = "saveType")String saveType) {
//		return MessageStorageHandler.getInstance().retireMessage(appId, deviceId, saveType);
		return "";
	}
	
	/**
	 * @return 
	 * @Description: 确认 回复FF00
	 */
	@RequestMapping(value = "/message/confirm", method = RequestMethod.GET)
	@ResponseBody
	public String confirm(@RequestParam(value = "appId")String appId, @RequestParam(value = "deviceId")String deviceId, @RequestParam(value = "saveType")String saveType) {
//		return MessageStorageHandler.getInstance().retireMessage(appId, deviceId, saveType);
		return "";
	}
}
