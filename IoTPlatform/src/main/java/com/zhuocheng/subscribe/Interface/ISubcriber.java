package com.zhuocheng.subscribe.Interface;

import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.subscribe.SubscribePublish;

/**
 * @Description: 订阅者接口
 * @author: leijing
 * @date: 2016年9月29日 下午5:07:20
 */
public interface ISubcriber<M> {
	/**
	 * @Description: 订阅
	 * @param: subscribePublish
	 *             订阅器
	 */
	public void subcribe(SubscribePublish subscribePublish);

	/**
	 * @Description: 退订
	 * @param: subscribePublish
	 *             订阅器
	 */
	public void unSubcribe(SubscribePublish subscribePublish);

	/**
	 * @Description: 接收消息
	 * @param: profileId
	 * @param: message
	 *             消息
	 * @param: subcriberType
	 *             订阅类型
	 * @param: subcriberId
	 *             订阅Id
	 * @return 
	 * @throws HttpRequestException 
	 * @throws ProfileHandleException 
	 */
	public String update(String profileId, M message, String subcriberType, String subcriberId, String type, String serviceId, String commandType) throws HttpRequestException, ProfileHandleException;
}