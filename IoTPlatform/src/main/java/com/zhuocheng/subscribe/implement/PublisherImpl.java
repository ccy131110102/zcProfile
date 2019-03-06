package com.zhuocheng.subscribe.implement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.subscribe.SubscribePublish;
import com.zhuocheng.subscribe.Interface.IPublisher;

/**
 * @Description: 发布者实现类
 */
public class PublisherImpl<M> implements IPublisher<M> {
	private String name;

	private Logger logger = Logger.getLogger(PublisherImpl.class);
	
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	
	public PublisherImpl(String name) {
		super();
		this.name = name;
	}

	// 发布方法，使用线程异步执行，防止并发阻塞
	public void publish(final SubscribePublish subscribePublish, final M message, final boolean isInstantMsg, final String subcriberType,
			final String subcriberId, final String type, final String serviceId, final String commandType) {
		cachedThreadPool.execute(new Runnable() {

			public void run() {
				try {
					subscribePublish.publish("", message, isInstantMsg, subcriberType, subcriberId, type, serviceId, commandType);
				} catch (HttpRequestException e) {
					// 写错误日志
					logger.error(e.getMessage());
				} catch (ProfileHandleException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				} catch (Exception e){
					logger.error(e.getMessage());
				}

			}
		});
	}
}
