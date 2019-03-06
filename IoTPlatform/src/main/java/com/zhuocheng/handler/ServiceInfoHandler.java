package com.zhuocheng.handler;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ServiceInfoHandler {
	private JedisPool jedisPool;

	// 单例对象
	private static ServiceInfoHandler instance = null;

	// 屏蔽构造函数
	private ServiceInfoHandler(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public static void init(JedisPool jedisPool) {
		if (instance == null) {

			if (instance == null) {
				instance = new ServiceInfoHandler(jedisPool);
			}

			instance.jedisPool = jedisPool;

		}
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static ServiceInfoHandler getInstance() {

		return instance;
	}

	public void saveServiceInfo(String services) {
		Jedis jedis = jedisPool.getResource();
		jedis.set(Constant.SERVERS_SAVEKEY, services);
		jedisPool.returnResource(jedis);
	}
	
	public String getServiceCallBackByServiceId(String serviceId) {
		Jedis jedis = jedisPool.getResource();
		Map serviceMap = (Map) JSONObject.parse(jedis.get(Constant.SERVERS_SAVEKEY), Feature.OrderedField);
//		jedis.close();
		
		String callBack = "58.59.64.11";
		if(serviceMap != null){
			Map serviceInfoMap = (Map) serviceMap.get(serviceId);
			
			callBack = (String) serviceInfoMap.get(Constant.SERVERS_CALLBACK);
		}
		
		jedisPool.returnResource(jedis);
		return callBack;
	}
}
