package com.zhuocheng.handler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.config.Config;

public class CacheHandler {

	private Map localMethodCache;
	private Map localPropertiesCache;
	private Map localDeviceCache;
	private Map localAppCache;

	// 单例对象
	private static CacheHandler instance = new CacheHandler();
	
	private static Config config = null;
    //声明redisso对象
    private static Redisson redisson = null;

	// 屏蔽构造函数
	private CacheHandler() {
	}

	public static void initRedisson(String ip, String port, String password){
		instance.config = new Config();
		instance.config.useSingleServer().setAddress(ip + ":" + port);
		instance.config.useSingleServer().setPassword(password);
		 redisson = (Redisson) Redisson.create(config);
	}
	
	public static void init(Map methodCache, Map propertiesCache, Map deviceCache, Map appCache) {
		CacheHandler.acquire("initLock");
		instance.localMethodCache = methodCache;
		instance.localPropertiesCache = propertiesCache;
		instance.localDeviceCache = deviceCache;
		instance.localAppCache = appCache;
		CacheHandler.release("initLock");
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static CacheHandler getInstance() {

		return instance;
	}

	public Map getLocalMethodCache() {
		while (CacheHandler.searchLock("initLock")) {

		}

		return localMethodCache;
	}

	public Map getLocalPropertiesCache() {
		while (CacheHandler.searchLock("initLock")) {

		}

		return localPropertiesCache;
	}

	public Map getLocalDeviceCache() {
		while (CacheHandler.searchLock("initLock")) {

		}

		return localDeviceCache;
	}

	public Map getLocalAppCache() {
		while (CacheHandler.searchLock("initLock")) {

		}

		return localAppCache;
	}

	//加锁
    public static void acquire(String lockName){
       //声明key对象
        String key = "LOCK" + lockName;
       //获取锁对象
        RLock lock = redisson.getLock(key);
       //加锁，并且设置锁过期时间，防止死锁的产生
        lock.lock(1, TimeUnit.SECONDS); 
        System.err.println("===" + lockName + "===lock======" + Thread.currentThread().getName());
    }
  //锁的释放
    public static void release(String lockName){
       //必须是和加锁时的同一个key
        String key = "LOCK" + lockName;
       //获取所对象
        RLock lock = redisson.getLock(key);
      //释放锁（解锁）
        lock.unlock();
        System.err.println("===" + lockName + "===unlock======"+Thread.currentThread().getName());
    }
    // 查询是否锁
    public static boolean searchLock(String lockName){
       //必须是和加锁时的同一个key
        String key = "LOCK" + lockName;
       //获取所对象
        RLock lock = redisson.getLock(key);
      //释放锁（解锁）
        return lock.isLocked();
    }
}
