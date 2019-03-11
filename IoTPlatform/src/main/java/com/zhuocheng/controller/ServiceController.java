package com.zhuocheng.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.AppInfoHandler;
import com.zhuocheng.handler.CacheHandler;
import com.zhuocheng.handler.MessageStorageHandler;
import com.zhuocheng.handler.ProfileHandler;
import com.zhuocheng.handler.ServiceInfoHandler;
import com.zhuocheng.mapper.CommandMapper;
import com.zhuocheng.processor.EncodeProcessor;
import com.zhuocheng.processor.factory.ProcessorFactory;
import com.zhuocheng.subscribe.SubscribePublish;
import com.zhuocheng.subscribe.Interface.ISubcriber;
import com.zhuocheng.subscribe.implement.SubcriberImpl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: 服务控制器，响应服务请求
 */
@Controller
public class ServiceController {
	@Autowired
	JedisPool jedisPool;
	@Autowired
	private CommandMapper CommandMapper;

	private Logger logger = Logger.getLogger(ServiceController.class);

	/**
	 * @Description: 用于服务器收集消息的订阅器订阅
	 */
	@PostConstruct
	public void serviceSubscribe() {
		// 用于服务中缓存当前最新的profile结构
		Map localCacheMap = new LinkedHashMap();
		Map localMethodCacheMap = new LinkedHashMap();
		Map localPropertiesCacheMap = new LinkedHashMap();
		
		// 第一次生成实例，初始化对象
		MessageStorageHandler.init(jedisPool, CommandMapper);
		ServiceInfoHandler.init(jedisPool);
		AppInfoHandler.init(jedisPool);

		SubscribePublish.getInstance().clearSubcriber();
		Jedis jedis = jedisPool.getResource();
		Map profileMap = (Map) jedis.hgetAll(Constant.CONSTRUCTION_SAVEKEY);
		Map deviceMap = (Map) jedis.hgetAll(Constant.CONSTRUCTION_DEVICE_SAVEKEY);
		Map appMap = (Map) JSONObject.parse(jedis.get(Constant.APP_SAVEKEY), Feature.OrderedField);
		// jedis.close();

		// 如果当前系统中没有任何的profile结构体，则抛出异常
		if (profileMap == null) {
			logger.error("没有注册任何profile");
			// 写错误日志
			jedisPool.returnResource(jedis);
			return;
		}

		// 获取profile结构体，按method向消息中心注册等待消息发布同时将profile信息缓存到服务本地
		// 如果远端profile被更新则需要调用此方法重新加载profile
		Iterator<String> profileIt = profileMap.keySet().iterator();
		while (profileIt.hasNext()) {
			String profileKey = profileIt.next();
			Map profile = (Map) JSONObject.parse((String) profileMap.get(profileKey), Feature.OrderedField);

			Iterator<String> serverIt = profile.keySet().iterator();
			while (serverIt.hasNext()) {
				String currentKey = serverIt.next();

				Map currentServerMap = (Map) profile.get(currentKey);
				Map currentMethodsMap = (Map) currentServerMap.get(Constant.PUBLISH_TYPE_METHOD);
				if (currentMethodsMap != null) {
					Iterator<String> methodsKeyIt = currentMethodsMap.keySet().iterator();

					while (methodsKeyIt.hasNext()) {
						String currentMethodsKey = methodsKeyIt.next();
						
						Map tempMap = (Map) currentMethodsMap.get(currentMethodsKey);
						String currentfCommand = (String) tempMap.get(Constant.PARAS_COMMAND);
						tempMap.put(Constant.METHOD_NAME, currentMethodsKey);
						tempMap.put(Constant.PROFILE_LISTKEY, currentKey);
						tempMap.put(Constant.PROFILE_KEY, profileKey);
						
						localMethodCacheMap.put(currentfCommand, tempMap);
						
						ISubcriber<String> subcriber = new SubcriberImpl<String>(profileKey, currentMethodsKey,
								currentKey, Constant.PUBLISH_TYPE_METHOD);
						SubscribePublish.getInstance().subcribe(subcriber);
					}
				}

				Map currentPropertiessMap = (Map) currentServerMap.get(Constant.PUBLISH_TYPE_PROPERTIES);
				if (currentPropertiessMap != null) {
					Iterator<String> propertiesKeyIt = currentPropertiessMap.keySet().iterator();

					while (propertiesKeyIt.hasNext()) {
						String currentPropertiesKey = propertiesKeyIt.next();
						
						Map tempMap = (Map) currentPropertiessMap.get(currentPropertiesKey);
						String currentCommand = (String) tempMap.get(Constant.PROPERTIES_COMMAND);
						tempMap.put(Constant.METHOD_NAME, currentPropertiesKey);
						tempMap.put(Constant.PROFILE_LISTKEY, currentKey);
						tempMap.put(Constant.PROFILE_KEY, profileKey);
						
						localPropertiesCacheMap.put(currentCommand, tempMap);
						
						ISubcriber<String> subcriber = new SubcriberImpl<String>(profileKey, currentPropertiesKey,
								currentKey, Constant.PUBLISH_TYPE_PROPERTIES);
						SubscribePublish.getInstance().subcribe(subcriber);
					}
				}
			}
		}
		
		CacheHandler.init(localMethodCacheMap, localPropertiesCacheMap, deviceMap, appMap);
		jedisPool.returnResource(jedis);
	}

	/**
	 * @Description: 导入profile结构体
	 */
	@RequestMapping(value = "/profileRegister", method = RequestMethod.POST, produces = "application/json;application/json;charset=UTF-8")
	@ResponseBody
	public String profileRegister(@RequestBody String content) {
		Jedis je = jedisPool.getResource();
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();

		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		String appId = "";
		String profileName = "";
		String profileContent = "";

		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);
		try {
			Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);

			appId = (String) paramsMap.get("appId");
			profileName = (String) paramsMap.get("profileName");

			if (!AppInfoHandler.getInstance().appIsExist(appId)) {
				// 如果当前欲绑定的APP不存在，则返回异常编码
				errorCode = Constant.APP_OP_ERROR_NOTEXIST_CODE;
				errorInfo = Constant.APP_OP_ERROR_NOTEXIST_MSG;
			} else {
				// 存储PROFILE结构
				profileContent = JSONObject.toJSONString(paramsMap.get("profileContent"));

				AppInfoHandler.getInstance().addProfileToApp(appId, profileName);

				je.hset("PROFILE", profileName, profileContent);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage());
			e.printStackTrace();
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		}

		resultBodyMap.put("profileId", profileName);
		resultBodyMap.put("profileName", profileName);

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resultBodyMap);

		this.serviceSubscribe();
		jedisPool.returnResource(je);
		return JSONObject.toJSONString(resultMap);
		// je.close();

		// System.out.println(JSONObject.toJSONString(jo.get("methods")));
	}

	/**
	 * @Description: app注册
	 */
	@RequestMapping(value = "/appResigter", method = RequestMethod.POST, produces = "application/json;application/json;charset=UTF-8")
	@ResponseBody
	public String appProfileRegister(@RequestBody String content) {
		Jedis je = jedisPool.getResource();
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();

		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		String appId = "";
		String name = "";

		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);

		try {
			Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);
			String appName = (String) paramsMap.get("appName");
			appId = appName;
			name = appName;
			resultBodyMap.put("appId", appId);
			resultBodyMap.put("appName", appName);

			if (AppInfoHandler.getInstance().appIsExist(appName)) {
				// 当前APP名称如果已经存在，则返回异常编码
				errorCode = Constant.APP_OP_ERROR_REGISTER_CODE;
				errorInfo = Constant.APP_OP_ERROR_REGISTER_MSG;
			} else {
				// 存储新的APP名称
				AppInfoHandler.getInstance().saveAppInfo(appName);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage());
			jedisPool.returnBrokenResource(je);
			e.printStackTrace();
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		}

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resultBodyMap);

		// je.set(Constant.APP_SAVEKEY, construction);

		this.serviceSubscribe();
		jedisPool.returnResource(je);
		return JSONObject.toJSONString(resultMap);

		// je.close();

		// System.out.println(JSONObject.toJSONString(jo.get("methods")));
	}

	/**
	 * @Description: callBackUrl注册
	 */
	@RequestMapping(value = "/callBackURLRegister", method = RequestMethod.POST, produces = "application/json;application/json;charset=UTF-8")
	@ResponseBody
	public String callBackURLRegister(@RequestBody String content) {
		Jedis je = jedisPool.getResource();
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();

		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		String appId = "";
		String profileId = "";
		Map urlMap;

		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);

		try {
			Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);
			appId = (String) paramsMap.get("appId");
			profileId = (String) paramsMap.get("profileId");
			urlMap = (Map) paramsMap.get("URL");
			resultBodyMap.put("appId", appId);
			resultBodyMap.put("profileId", profileId);

			AppInfoHandler.getInstance().saveCallBackURL(appId, profileId, urlMap);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage());
			jedisPool.returnResource(je);
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		}

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resultBodyMap);

		// je.set(Constant.APP_SAVEKEY, construction);

		this.serviceSubscribe();
		jedisPool.returnResource(je);
		return JSONObject.toJSONString(resultMap);

		// je.close();

		// System.out.println(JSONObject.toJSONString(jo.get("methods")));
	}

	/**
	 * @Description: 下发命令
	 */
	@RequestMapping(value = "/issueCommand", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String downCommand(@RequestBody String content) {
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();

		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;

		try {
			// 从传入数据中获取字段并生成查询关键字，用于获取profile
			Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);
			String appName = (String) paramsMap.get("appName");
			String deviceId = (String) paramsMap.get("deviceId");
			String appId = (String) paramsMap.get("appId");
			String profileId = (String) paramsMap.get("profileId");
			String commandKey = appId + "-" + profileId + "-" + deviceId;

			resultBodyMap.put("deviceId", deviceId);

			Map commandMap = paramsMap;
			commandMap.put("deviceId", commandKey);
			String command = JSONObject.toJSONString(commandMap);

			// 创建编码处理器，用于将下发参数转换为报文等待设备取暂存
			EncodeProcessor eprocessor = ProcessorFactory.createEncodeProcessor(command, jedisPool);
			String saveId = eprocessor.publish(eprocessor.combile((Map) JSONObject.parse(command, Feature.OrderedField)),
					Constant.PUBLISH_TYPE_METHOD);
			resultBodyMap.put("commandKey", saveId);

		} catch (HttpRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (ProfileHandleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorCode = Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_CODE;
			errorInfo = Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_MSG;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			e.printStackTrace();
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		}

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resultBodyMap);

		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * @Description: 根据appId和profileId查询profile结构
	 */
	@RequestMapping(value = "/getProfile", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getProfileByAppIdAndProfileId(@RequestBody String content) {
		Jedis jedis = jedisPool.getResource();

		// 从传入参数获取字段信息用于查询指定的profile
		Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);
		String appId = (String) paramsMap.get("appId");
		String profileId = (String) paramsMap.get("profileId");
		String target = (String) paramsMap.get("target");

		Map resultMap = new HashMap();
		Map resulBodytMap = new HashMap();
		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);
		
		// 判断传参是否正确，如果参数不全则返回异常编号
		if (appId == null || profileId == null) {
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		} else {

			String result = ProfileHandler.getInstance().getProfileByAppIdAndProfileId(appId, profileId);

			// 拼接返回报文体
			resulBodytMap.put("appId", appId);
			resulBodytMap.put("profileId", profileId);
			resulBodytMap.put("profile", (Map) JSONObject.parse(result, Feature.OrderedField));
		}

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resulBodytMap);

		jedisPool.returnResource(jedis);
		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * @Description: 服务注册
	 */
	@RequestMapping(value = "/serversRegister", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public void serversRegister(@RequestParam(value = "servers") String servers) {
		Jedis je = jedisPool.getResource();

		je.set("SERVERS", servers);
		jedisPool.returnResource(je);
	}

	/**
	 * @Description: 移除已订阅的业务服务
	 */
	@RequestMapping(value = "/serverRemoveSubscribe", method = RequestMethod.GET)
	public void serverRemoveSubscribe() {

	}

	/**
	 * @Description: 订阅平台数据管理
	 */
	@RequestMapping(value = "/dataManageSubscribe", method = RequestMethod.GET)
	public void dataManageSubscribe(@RequestParam(value = "profile") String profile) {

	}

	/**
	 * @Description: 移除已订阅平台数据管理
	 */
	@RequestMapping(value = "/dataManageRemoveSubscribe", method = RequestMethod.GET)
	public void dataManageRemoveSubscribe(@RequestParam(value = "profile") String profile) {

	}

}
