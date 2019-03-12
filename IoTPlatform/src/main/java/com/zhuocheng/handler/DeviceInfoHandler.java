package com.zhuocheng.handler;

import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: 设备处理处理类
 */
public class DeviceInfoHandler {
	private JedisPool jedisPool;

	// 单例对象
	private static DeviceInfoHandler instance = null;

	// 屏蔽构造函数
	private DeviceInfoHandler(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public static void init(JedisPool jedisPool) {
		if (instance == null) {

			if (instance == null) {
				instance = new DeviceInfoHandler(jedisPool);
			}

			instance.jedisPool = jedisPool;

		}
	}
	
	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static DeviceInfoHandler getInstance() {

		return instance;
	}
	
	/**
	 * @Description: 保存设备信息，若已存在则覆盖
	 * @param deviceInfo 设备信息键值对, profileId profile编号, deviceId 设备编号, appId 应用编号
	 */
	public String saveDeviceInfo(String deviceId, String profileId, String appId, Map deviceInfo){
		Jedis jedis = jedisPool.getResource();
		String key = generateKey(appId, profileId, deviceId);
		jedis.hset("DEVICE", key, JSONObject.toJSONString(deviceInfo));
		
		jedisPool.returnResource(jedis);
		return key;
	}
	
	/**
	 * @Description: 根据deviceId和appId获取指定的设备信息
	 * @param deviceId 设备唯一识别码
	 */
	public Map getDeviceInfoByDeviceKey(String deviceId){
		Map deviceInfo = (Map) JSONObject.parse((String)CacheHandler.getInstance().getLocalDeviceCache().get(deviceId));
		
		if(deviceInfo == null){
			return null;
		}
		
		return deviceInfo;
	}
	
	/**
	 * @Description: 根据deviceId设备信息
	 * @param deviceId
	 */
	public Map getDeviceInfoByDeviceId(String deviceId){
		Map deviceInfoMap = CacheHandler.getInstance().getLocalDeviceCache();
		
		if(deviceInfoMap == null){
			return null;
		}
		
		Iterator deviceIt = deviceInfoMap.keySet().iterator();
		
		while(deviceIt.hasNext()){
			String deviceKey = (String) deviceIt.next();
			
			Map deviceInfo = (Map) JSONObject.parse((String)deviceInfoMap.get(deviceKey));
			
			if(deviceInfo.get(Constant.DEVICEINFO_DEVICEID).equals(deviceId)){
				return deviceInfo;
			}
		}
		
		return null;
	}
	
	private String generateKey(String appId, String profileId, String deviceId){
		return appId + "-" + profileId + "-" +  deviceId;
	}
}
