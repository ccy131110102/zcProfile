package com.zhuocheng.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AppInfoHandler {
	private JedisPool jedisPool;

	// 单例对象
	private static AppInfoHandler instance = null;

	// 屏蔽构造函数
	private AppInfoHandler(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public static void init(JedisPool jedisPool) {
		if (instance == null) {

			if (instance == null) {
				instance = new AppInfoHandler(jedisPool);
			}

			instance.jedisPool = jedisPool;

		}
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static AppInfoHandler getInstance() {

		return instance;
	}

	/**
	 * @Description: 判断当前app是否存在
	 */
	public boolean appIsExist(String appName) {
		Jedis jedis = jedisPool.getResource();
		Map appsMap = (Map) JSONObject.parse(jedis.get("APP"), Feature.OrderedField);

		if(appsMap == null){
			return false;
		}
		
		jedisPool.returnResource(jedis);
		return appsMap.get(appName) != null;
	}

	/**
	 * @Description: 存储app信息
	 */
	public boolean saveAppInfo(String appName) {
		Jedis jedis = jedisPool.getResource();
		Map appsMap = (Map) JSONObject.parse(jedis.get("APP"), Feature.OrderedField);
		if(appsMap == null){
			appsMap = new HashMap();
		}
		
		appsMap.put(appName, new JSONArray());
		
		boolean result = jedis.set("APP", JSONObject.toJSONString(appsMap)) == "OK";
		jedisPool.returnResource(jedis);
		
		return result;
	}

	/**
	 * @Description: 向appId添加profileId进行关联
	 */
	public boolean addProfileToApp(String appId, String profileId) {
		Jedis jedis = jedisPool.getResource();
		Map appsMap = (Map) JSONObject.parse(jedis.get("APP"), Feature.OrderedField);
		JSONArray array = (JSONArray) appsMap.get(appId);
		array.add(profileId);
		appsMap.put(appId, array);

		boolean result = jedis.set("APP", JSONObject.toJSONString(appsMap)) == "OK";
		jedisPool.returnResource(jedis);
		
		return result;
	}

	/**
	 * @Description: 存储callBackURL
	 */
	public boolean saveCallBackURL(String appId, String profileId, Map urlMap) {
		Jedis jedis = jedisPool.getResource();
		boolean result = jedis.hset("SERVICE", appId + "-" + profileId, JSONArray.toJSONString(urlMap)) == 1;
		jedisPool.returnResource(jedis);
		return result;
	}

	/**
	 * @Description: 获取callBackURL
	 */
	public Map getCallBackURL(String appId, String profileId) {
//		Jedis jedis = jedisPool.getResource();
//		String resultMap = jedis.hget("SERVICE", appId + "-" + profileId);
//		
//		jedisPool.returnResource(jedis);
//		return (Map) JSONObject.parse(resultMap, Feature.OrderedField);
		
		return null;
	}

	/**
	 * @Description: 获取callBackURL
	 */
	public boolean sendStatusChange(String appId, String profileId, String deviceId, int status) {
		Jedis jedis = jedisPool.getResource();
		Map resultMap = new HashMap();

		resultMap.put("target", "issueCommand");
		resultMap.put("appId", appId);
		resultMap.put("profileId", profileId);
		resultMap.put("deviceId", deviceId);
		resultMap.put("commandKey", appId + "-" + profileId + "-" + deviceId);
		resultMap.put("commandStatus", status);

		String callbackUrl="";
		
		if (AppInfoHandler.getInstance().getCallBackURL(appId, profileId) != null) {
			callbackUrl = (String) AppInfoHandler.getInstance().getCallBackURL(appId, profileId)
					.get(Constant.CALLBACK_ISSUECOMMANDSTATUSCHANGE);
		}else{
			callbackUrl = Constant.LOCAL_STORAGESERVICE_URL;
		}
		try {
			HttpRequest httpRequest = new HttpRequest();
			String responseStr = httpRequest.sendPost(callbackUrl, JSONObject.toJSONString(resultMap));
		} catch (Exception e) {
			// TODO: handle exception
			jedisPool.returnBrokenResource(jedis);;
			return false;
		}

		jedisPool.returnResource(jedis);
		return true;
	}
}
