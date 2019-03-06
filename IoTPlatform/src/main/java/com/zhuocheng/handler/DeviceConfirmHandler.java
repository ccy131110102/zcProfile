package com.zhuocheng.handler;

import java.util.HashMap;
import java.util.Map;

import com.zhuocheng.mapper.CommandMapper;
import com.zhuocheng.model.Command;

import redis.clients.jedis.JedisPool;

public class DeviceConfirmHandler {
	private CommandMapper CommandMapper;
	
	private JedisPool jedisPool;
	
	private Map<String, Map<String, Integer>> messageMap;
	 

	// 单例对象
	private static DeviceConfirmHandler instance = null;

	private DeviceConfirmHandler() {
		messageMap = new HashMap<String, Map<String, Integer>> ();
	}

	public static void init(JedisPool jedisPool, CommandMapper commandMapper) {
		if (instance == null) {

			if (instance == null) {
				instance = new DeviceConfirmHandler();
			}

			instance.jedisPool = jedisPool;
			instance.CommandMapper = commandMapper;
		}
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static DeviceConfirmHandler getInstance() {
		// 延迟加载
		if (instance == null) {
			// 同步锁
			synchronized (ProfileHandler.class) {
				if (instance == null)
					instance = new DeviceConfirmHandler();
			}
		}

		return instance;
	}
	
	/**
	 * @Description: 保存等待确认的报文
	 */
	public void saveMessageToConfirm(String messageId, String appId, String deviceId, int primaryId, int state){
		Map<String, Integer> messageState = new HashMap<String, Integer>();
		
		messageState.put("primaryId", primaryId);
		messageState.put("state", state);
		
		String key = appId + "-" + deviceId + "-" + messageId;
		
		messageMap.put(key, messageState);
	}
	
	/**
	 * @Description: 根据messageId、appId、deviceId查询是否有等待报文
	 */
	public boolean hasMessageToConfirm(String messageId, String appId, String deviceId){
		String key = appId + "-" + deviceId + "-" + messageId;
		
		return messageMap.containsKey(key);
	}
	
	/**
	 * @Description: 根据messageId、appId、deviceId确认报文并修改状态
	 */
	public boolean confirmMessage(String messageId, String profileId, String appId, String deviceId){
		String key = appId + "-" + deviceId + "-" + messageId;
		
		Map<String, Integer> messageState = messageMap.get(key);
		
		int primaryId = messageState.get("primaryId");
		int state = messageState.get("state") + 1;
		
		Command command = new Command();
		command.setCommandId(primaryId);
		command.setCommandState(state);
		
		boolean result = CommandMapper.updateCommandState(command) > 0;
		
		AppInfoHandler.getInstance().sendStatusChange(appId, profileId, deviceId, state);
		
		if(result){
			messageMap.remove(key);
		}
		
		return result;
	}
}
