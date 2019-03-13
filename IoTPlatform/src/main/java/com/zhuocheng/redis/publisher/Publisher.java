package com.zhuocheng.redis.publisher;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Publisher{

	private final JedisPool jedisPool;

	public Publisher(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public void pub(){
		Jedis jedis = jedisPool.getResource(); // 连接池中取出一个连接
		jedis.publish("registerChannel", "refresh"); // 从 registerChannel
		jedis.close();
	}
}