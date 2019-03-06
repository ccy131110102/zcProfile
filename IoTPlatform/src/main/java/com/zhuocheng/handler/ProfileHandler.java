package com.zhuocheng.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.ProfileHandleException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: profile处理工具类
 */
public class ProfileHandler {
	private JedisPool jedisPool;

	// 单例对象
	private static ProfileHandler instance = null;

	// 屏蔽构造函数
	private ProfileHandler(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static ProfileHandler getInstance() {
		return instance;
	}

	public static void init(JedisPool jedisPool) {
		if (instance == null) {

			if (instance == null) {
				instance = new ProfileHandler(jedisPool);
			}

		}
	}

	/**
	 * @Description: 保存profile结构体，如果profile已经存在则进行覆盖操作
	 * @param profile
	 */
	public boolean saveProfile(Map profile) {
		jedisPool.getResource().hmset(Constant.CONSTRUCTION_SAVEKEY, profile);
		return true;
	}

	/**
	 * @Description: 根据指定级别更新profile结构体，如果结构体不存在则抛出异常
	 */
	public boolean updateProfile() {
		return true;
	}

	/**
	 * @Description: 根据指定级别移除profile结构体
	 */
	public boolean delProfile() {
		return true;
	}

	/**
	 * @throws ProfileHandleException
	 * @Description: 根据指定级别获取profile结构体
	 */
	// public Map getProfileByLevel(String level, String key) throws
	// ProfileHandleException {
	// Map result = null;
	//
	// Jedis jedis = jedisPool.getResource();
	// Map profile = (Map) (jedis.hmget(Constant.CONSTRUCTION_SAVAKEY));
	// Iterator<String> serverIt = profile.keySet().iterator();
	//
	// // 如果当前系统中没有任何的profile结构体，则抛出异常
	// if (profile == null) {
	// throw new
	// ProfileHandleException(Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_MSG,
	// Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_CODE);
	// }
	//
	// switch (level) {
	// case Constant.CONSTRUCTION_OPLEVEL_SERVER:
	//
	// while (serverIt.hasNext()) {
	// String currentKey = serverIt.next();
	//
	// if (currentKey.equals(key)) {
	// result = (Map) profile.get(key);
	// break;
	// }
	// }
	//
	// break;
	//
	// case Constant.CONSTRUCTION_OPLEVEL_METHODS:
	//
	// while (serverIt.hasNext()) {
	// String currentKey = serverIt.next();
	//
	// Map currentServerMap = (Map) profile.get(key);
	// Map currentMethodsMap = (Map) currentServerMap.get("methods");
	// if (currentMethodsMap != null) {
	// Iterator<String> methodsKeyIt = currentMethodsMap.keySet().iterator();
	//
	// while (methodsKeyIt.hasNext()) {
	// String currentMethodsKey = methodsKeyIt.next();
	// Map methodMap = (Map) currentMethodsMap.get(currentMethodsKey);
	//
	// if (methodMap.get("commandId").equals(key)) {
	// Map tempResult = new HashMap();
	// tempResult.put(currentKey, methodMap);
	//
	// result = tempResult;
	// break;
	// }
	// }
	// }
	// }
	//
	// break;
	//
	// case Constant.CONSTRUCTION_OPLEVEL_PROPERTIES:
	//
	// while (serverIt.hasNext()) {
	// String currentKey = serverIt.next();
	//
	// Map currentServerMap = (Map) profile.get(key);
	// Map currentPropertiesMap = (Map) currentServerMap.get("properties");
	// if (currentPropertiesMap != null) {
	// Iterator<String> propertiesKeyIt =
	// currentPropertiesMap.keySet().iterator();
	//
	// while (propertiesKeyIt.hasNext()) {
	// String currentpropertiesKey = propertiesKeyIt.next();
	// Map propertyMap = (Map) currentPropertiesMap.get(currentpropertiesKey);
	//
	// if (currentpropertiesKey.equals(key)) {
	// Map tempResult = new HashMap();
	// tempResult.put(currentKey, propertyMap);
	//
	// result = tempResult;
	// break;
	// }
	// }
	// }
	// }
	//
	// break;
	//
	// default:
	//
	// break;
	// }
	//
	// return result;
	// }

	/**
	 * @throws ProfileHandleException
	 * @Description: 根据commandId获取解码profile结构体
	 */
	public Map getDecodeProfileByMessageId(String commandId) throws ProfileHandleException {
		Map result = null;

		Jedis jedis = jedisPool.getResource();
		Map profileMap = (Map) jedis.hgetAll(Constant.CONSTRUCTION_SAVEKEY);

		// 如果当前系统中没有任何的profile结构体，则抛出异常
		if (profileMap == null) {
			throw new ProfileHandleException(Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_MSG,
					Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_CODE);
		}

		Iterator<String> profileIt = profileMap.keySet().iterator();
		while (profileIt.hasNext()) {
			String profileKey = profileIt.next();
			Map profile = (Map) JSONObject.parse((String) profileMap.get(profileKey), Feature.OrderedField);

			Iterator<String> serverIt = profile.keySet().iterator();
			while (serverIt.hasNext()) {
				String currentKey = serverIt.next();

				Map currentServerMap = (Map) profile.get(currentKey);
				Map currentMethodsMap = (Map) currentServerMap.get("properties");
				if (currentMethodsMap != null) {
					Iterator<String> methodsKeyIt = currentMethodsMap.keySet().iterator();

					while (methodsKeyIt.hasNext()) {
						String currentMethodsKey = methodsKeyIt.next();
						Map methodMap = (Map) currentMethodsMap.get(currentMethodsKey);

						if (methodMap != null && methodMap.get("commandId").equals(commandId)) {
							Map tempMethod = new HashMap();
							Map tempProfile = new HashMap();
							Map tempApp = new HashMap();
							Map tempResult = new HashMap();
							tempMethod.put(currentMethodsKey, methodMap);
							tempProfile.put(currentKey, tempMethod);
							tempApp.put(profileKey, tempProfile);

							Map appProfileMap = (Map) JSONObject.parse(jedis.get(Constant.APP_SAVEKEY), Feature.OrderedField);
							String appId = "DEFAULT";

							Iterator<String> appIt = appProfileMap.keySet().iterator();
							while (appIt.hasNext()) {
								String appKey = appIt.next();
								JSONArray profileIdList = (JSONArray) appProfileMap.get(appKey);

								if (profileIdList.contains(profileKey)) {
									appId = appKey;
								}
							}

							tempResult.put(appId, tempApp);
							result = tempResult;
							break;
						}
					}
				}
			}
		}

		jedisPool.returnResource(jedis);
		return result;
	}

	/**
	 * @throws ProfileHandleException
	 * @Description: 根据serviceId和method获取编码profile结构体
	 */
	public Map getEncodeProfileByMethod(String serviceId, String method, String profileId, String appId)
			throws ProfileHandleException {
		Map result = null;

		Jedis jedis = jedisPool.getResource();
		Map profileMap = (Map) (jedis.hgetAll(Constant.CONSTRUCTION_SAVEKEY));

		// 如果当前系统中没有任何的profile结构体，则抛出异常
		if (profileMap == null) {
			throw new ProfileHandleException(Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_MSG,
					Constant.CONSTRUCTION_OP_ERROR_NOPROFILE_CODE);
		}

		Iterator<String> profileIt = profileMap.keySet().iterator();
		while (profileIt.hasNext()) {
			String profileKey = profileIt.next();
			if (profileKey.equals(profileId)) {
				Map profile = (Map) JSONObject.parse((String) profileMap.get(profileKey), Feature.OrderedField);

				Iterator<String> serverIt = profile.keySet().iterator();
				while (serverIt.hasNext()) {
					String currentKey = serverIt.next();

					if (currentKey.equals(serviceId)) {
						Map currentServerMap = (Map) profile.get(currentKey);
						Map currentPropertiesMap = (Map) currentServerMap.get("methods");
						if (currentPropertiesMap != null) {
							Iterator<String> propertiesKeyIt = currentPropertiesMap.keySet().iterator();

							while (propertiesKeyIt.hasNext()) {
								String currentpropertiesKey = propertiesKeyIt.next();
								Map propertyMap = (Map) currentPropertiesMap.get(currentpropertiesKey);

								if (currentpropertiesKey.equals(method)) {
									Map temp = new HashMap();
									temp.put(currentpropertiesKey, propertyMap);

									Map tempResult = new HashMap();
									tempResult.put(currentKey, temp);
									result = tempResult;
									break;
								}
							}
						}
					}
				}
			}
		}

		jedisPool.returnResource(jedis);
		return result;
	}

	/**
	 * @Description: 根据AppId获取ProfileId
	 */
	public String getAppIdByProfileId(String ProfileId) throws ProfileHandleException {
		String appId = null;

		Jedis jedis = jedisPool.getResource();
		Map appMap = (Map) JSONObject.parse(jedis.get(Constant.APP_SAVEKEY), Feature.OrderedField);

		Iterator appIt = appMap.keySet().iterator();
		while (appIt.hasNext()) {

			String currentAppId = (String) appIt.next();

			JSONArray profileIdArray = (JSONArray) appMap.get(currentAppId);

			for (Object profileId : profileIdArray) {
				if (ProfileId.equals(profileId)) {
					appId = currentAppId;
					break;
				}
			}
		}

		jedisPool.returnResource(jedis);
		return appId;

	}
	
	/**
	 * @Description: 根据appId和profileId查询profile结构
	 */
	public String getProfileByAppIdAndProfileId(String appId, String profileId){
		Jedis jedis = jedisPool.getResource();
		
		Map appMap = (Map) JSONObject.parse(jedis.get("APP"), Feature.OrderedField);
		Iterator appIt = appMap.keySet().iterator();
		while(appIt.hasNext()){
			String appSaveId = (String) appIt.next();
			
			if(appId.equals(appSaveId)){
				JSONArray profileArray = (JSONArray) appMap.get(appSaveId);
				
				for(Object profileSaveId : profileArray){
					if(profileId.equals(profileSaveId)){
						jedisPool.returnResource(jedis);
						return jedis.hget("PROFILE", profileId);
					}
				}
			}
		}
		
		jedisPool.returnResource(jedis);
		return "";
	}
}
