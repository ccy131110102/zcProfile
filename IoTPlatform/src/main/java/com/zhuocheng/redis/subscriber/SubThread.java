package com.zhuocheng.redis.subscriber;

import com.zhuocheng.controller.ServiceController;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SubThread extends Thread {
	
	private JedisPool jedisPool;
	private ServiceController sc;
	
	public SubThread(JedisPool jedisPool, ServiceController sc) {
		this.jedisPool = jedisPool;
		this.sc = sc;
	}
	
	@Override
	public void run() {
		Jedis jedis = jedisPool.getResource(); // 取出一个连接
		jedis.subscribe(new Subscriber(sc), "registerChannel"); // 通过subscribe
																// 的api去订阅，入参是订阅者和频道名
		jedis.close();
	}
}