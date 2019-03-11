package com.zhuocheng.subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.ProfileHandler;
import com.zhuocheng.subscribe.Interface.ISubcriber;

import redis.clients.jedis.JedisPool;

/**
 * @Description: 订阅器类
 */
public class SubscribePublish<M> {
	// 订阅器名称
	private String serverId;
	// 单例对象
	private static SubscribePublish instance = null;
	// 订阅器队列容量
	final int QUEUE_CAPACITY = 20;
	// 订阅器存储队列
	private BlockingQueue<Msg> queue = new ArrayBlockingQueue<Msg>(QUEUE_CAPACITY);
	// 订阅者
	private List<ISubcriber> subcribers = new ArrayList<ISubcriber>();

	private Logger logger = Logger.getLogger(SubscribePublish.class);
	
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

	/**
	 * @Description:构造方法
	 * @param name
	 */
	SubscribePublish(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static SubscribePublish getInstance() {
		// 延迟加载
		if (instance == null) {
			// 同步锁
			synchronized (ProfileHandler.class) {
				if (instance == null)
					instance = new SubscribePublish("");
			}
		}

		System.out.println(instance.subcribers.size());
		
		return instance;
	}

	/**
	 * @Description: 接收发布者的消息
	 * @param profileId
	 * @param Msg
	 * @param isInstantMsg
	 * @throws HttpRequestException 
	 * @throws ProfileHandleException 
	 */
	public String publish(String profileId, M message, boolean isInstantMsg, String subcriberType, String subcriberId, String type, String serviceId, String commandType) throws HttpRequestException, ProfileHandleException {
		cachedThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					update(profileId, message, subcriberType, subcriberId, type, serviceId, commandType);
				} catch (HttpRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getStackTrace());
				} catch (ProfileHandleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getStackTrace());
				}
			}
		});
		
		return "";
	}

	/**
	 * @Description: 订阅
	 * @param subcriber
	 * @return: void
	 */
	public void subcribe(ISubcriber subcriber) {
		subcribers.add(subcriber);
	}

	/**
	 * @Description: 退订
	 * @param subcriber
	 * @return: void
	 */
	public void unSubcribe(ISubcriber subcriber) {
		subcribers.remove(subcriber);
	}

	/**
	 * @Description: 发送存储队列所有消息
	 * @return: void
	 * @throws HttpRequestException 
	 * @throws ProfileHandleException 
	 */
	public void update() throws HttpRequestException, ProfileHandleException {
		Msg m = null;
		while ((m = queue.poll()) != null) {
			this.update(m.getPublisher(), (M) m.getMsg(), m.getSubcriberType(), m.getSubcriberId(), m.getType(), m.getServiceId(), m.getCommandType());
		}
	}

	/**
	 * @Description: 发送消息
	 * @param profileId
	 * @param Msg
	 * @return: void
	 * @throws HttpRequestException 
	 * @throws ProfileHandleException 
	 */
	public String update(String profileId, M Msg, String subcriberType, String subcriberId, String type, String serviceId, String commandType) throws HttpRequestException, ProfileHandleException {
		String saveId = "";
		
		for (ISubcriber subcriber : subcribers) {
			String temp = subcriber.update(profileId, Msg, subcriberType, subcriberId, type, serviceId, commandType);
			
			if(temp != ""){
				saveId = temp;
				break;
			}
		}
		
		return saveId;
	}
	
	/**
	 * @Description: 清空订阅队列
	 */
	public void clearSubcriber() {
		subcribers.clear();
	}
}

/**
 * @Description: 消息类
 */
class Msg<M> {
	private String publisher;
	private String subcriberType;
	private String subcriberId;
	private String type;
	private String serviceId;
	private String commandType;
	private M m;

	public Msg(String publisher, M m, String subcriberType, String subcriberId, String type, String serviceId, String commandType) {
		this.publisher = publisher;
		this.subcriberType = subcriberType;
		this.subcriberId = subcriberId;
		this.type = type;
		this.serviceId = serviceId;
		this.commandType = commandType;
		this.m = m;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public M getMsg() {
		return m;
	}

	public void setMsg(M m) {
		this.m = m;
	}

	public String getSubcriberType() {
		return subcriberType;
	}

	public void setSubcriberType(String subcriberType) {
		this.subcriberType = subcriberType;
	}

	public String getSubcriberId() {
		return subcriberId;
	}

	public void setSubcriberId(String subcriberId) {
		this.subcriberId = subcriberId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getCommandType() {
		return commandType;
	}

	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}
	
	
}
