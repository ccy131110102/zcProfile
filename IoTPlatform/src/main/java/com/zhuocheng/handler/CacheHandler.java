package com.zhuocheng.handler;

import java.util.Map;

public class CacheHandler {

	private Map localMethodCache;
	private Map localPropertiesCache;
	private Map localDeviceCache;
	private Map localAppCache;

	// 单例对象
	private static CacheHandler instance = null;

	public static void init(Map methodCache, Map propertiesCache, Map deviceCache, Map appCache) {
		if (instance == null) {

			if (instance == null) {
				instance = new CacheHandler();
			}

			instance.localMethodCache = methodCache;
			instance.localPropertiesCache = propertiesCache;
			instance.localDeviceCache = deviceCache;
			instance.localAppCache = appCache;
		}
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static CacheHandler getInstance() {

		return instance;
	}

	public Map getLocalMethodCache() {
		return localMethodCache;
	}

	public Map getLocalPropertiesCache() {
		return localPropertiesCache;
	}

	public Map getLocalDeviceCache() {
		return localDeviceCache;
	}

	public Map getLocalAppCache() {
		return localAppCache;
	}
}
