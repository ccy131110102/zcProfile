package com.zhuocheng.handler;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPool;

public class CacheHandler {

	private Map localMethodCache;
	private Map localPropertiesCache;
	private Map localDeviceCache;
	private Map localAppCache;

	private Set<String> downLock;
	private boolean initLock;

	// 单例对象
	private static CacheHandler instance = null;

	// 屏蔽构造函数
	private CacheHandler() {
	}

	public static void init(Map methodCache, Map propertiesCache, Map deviceCache, Map appCache) {
		CacheHandler.instance = new CacheHandler();
		instance.initLock = true;
		instance.localMethodCache = methodCache;
		instance.localPropertiesCache = propertiesCache;
		instance.localDeviceCache = deviceCache;
		instance.localAppCache = appCache;
		instance.initLock = false;
		instance.downLock = new LinkedHashSet<String>();
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static CacheHandler getInstance() {

		return instance;
	}

	public Map getLocalMethodCache() {
		while (this.initLock) {

		}

		return localMethodCache;
	}

	public Map getLocalPropertiesCache() {
		while (this.initLock) {

		}

		return localPropertiesCache;
	}

	public Map getLocalDeviceCache() {
		while (this.initLock) {

		}

		return localDeviceCache;
	}

	public Map getLocalAppCache() {
		while (this.initLock) {

		}

		return localAppCache;
	}

	/**
	 * @Description: 当下发指令时，将进行下发动作的设备加读写锁
	 * @param deviceKey
	 *            设备key
	 */
	public boolean getDownLock(String deviceKey) {
		synchronized (this) {
			return downLock.add(deviceKey);
		}
	}

	/**
	 * @Description: 解除指定设备的锁
	 * @param deviceKey
	 *            设备key
	 */
	public void returnDownLock(String deviceKey) {
		if (downLock.contains(deviceKey)) {
			downLock.remove(deviceKey);
		}
	}

	/**
	 * @Description: 查询当前设备是否有锁
	 * @param deviceKey
	 *            设备key
	 */
	public boolean canGetDownLock(String deviceKey) {
		return downLock.contains(deviceKey);
	}
}
