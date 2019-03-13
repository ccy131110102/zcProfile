package com.zhuocheng.redis.subscriber;

import com.zhuocheng.controller.ServiceController;

import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {
	ServiceController sc;
	
	public Subscriber(ServiceController sc) {
		this.sc = sc;
	}
	
	@Override
	public void onMessage(String channel, String message) { // 收到消息会调用
		sc.serviceSubscribe();
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) { // 订阅了频道会调用
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) { // 取消订阅
																		// 会调用

	}
}