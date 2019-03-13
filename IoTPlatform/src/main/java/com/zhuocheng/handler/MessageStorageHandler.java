package com.zhuocheng.handler;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.mapper.CommandMapper;
import com.zhuocheng.model.Command;
import com.zhuocheng.util.SerializeUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: 消息暂存区操作类
 */
public class MessageStorageHandler {
	private CommandMapper CommandMapper;

	private JedisPool jedisPool;

	// 单例对象
	private static MessageStorageHandler instance = null;

	private MessageStorageHandler() {

	}

	public static void init(JedisPool jedisPool, CommandMapper commandMapper) {
		if (instance == null) {

			if (instance == null) {
				instance = new MessageStorageHandler();
			}

			instance.jedisPool = jedisPool;
			instance.CommandMapper = commandMapper;
		}
	}

	/**
	 * @Description: 单例模式获取单例对象
	 */
	public static MessageStorageHandler getInstance() {
		// 延迟加载
		if (instance == null) {
			// 同步锁
			synchronized (ProfileHandler.class) {
				if (instance == null)
					instance = new MessageStorageHandler();
			}
		}

		return instance;
	}

	/**
	 * @Description: 将消息存储至暂存区
	 */
	public String saveMessage(String message, String appId, String profileId, String deviceId, String saveType,
			String state, String serviceId, String serviceType, String commandType) {
		Jedis jedis = jedisPool.getResource();
		Queue queue = null;

		// 获取读写锁
		CacheHandler.acquire(generateKey(appId, profileId, deviceId));

		if (SerializeUtil.unserialize(retireMessageQueue(appId, profileId, deviceId, saveType)) == null) {
			queue = new LinkedBlockingQueue();
		} else {
			queue = (LinkedBlockingQueue) SerializeUtil
					.unserialize(retireMessageQueue(appId, profileId, deviceId, saveType));
		}

		Command command = new Command();
		command.setCommandContent(message);
		command.setCommandDeviceId(deviceId);
		command.setCommandDirection(saveType);
		command.setCommandState(Integer.valueOf(state));
		command.setCommandAppId(appId);
		command.setCommandProfileId(profileId);
		command.setCommandServiceId(serviceId);
		command.setCommandServiceType(serviceType);
		command.setCommandType(commandType);
		CommandMapper.insertOneCommandInfo(command);
		System.out.println(command.getCommandId());

		System.out.println(message);
		Map messageMap = ((Map) JSONObject.parse(message, Feature.OrderedField));
		messageMap.put("primaryId", command.getCommandId());

		if (state.equals("0") && !saveType.equals(Constant.COMMAND_SAVETYPE_UP)) {
			queue.offer(JSONObject.toJSONString(messageMap));

			jedis.hset(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes(),
					SerializeUtil.serialize(queue));
		}

		// 解锁
		CacheHandler.release(generateKey(appId, profileId, deviceId));

		jedisPool.returnResource(jedis);
		return String.valueOf(command.getCommandId());
	}

	/**
	 * @Description: 取暂存数据
	 */
	public String retireMessage(String messageId, String appId, String profileId, String deviceId, String saveType) {
		Jedis jedis = jedisPool.getResource();
		String result = null;

		// 获取读写锁
		CacheHandler.acquire(generateKey(appId, profileId, deviceId));

		byte[] message = jedis.hget(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes());

		if (message != null) {
			Queue queue = (LinkedBlockingQueue) SerializeUtil.unserialize(message);
			result = String.valueOf(queue.peek());

			if (result != null && !queue.isEmpty()) {
				Map messageMap = (Map) JSONObject.parse(result, Feature.OrderedField);

				Command command = new Command();
				command.setCommandId((int) messageMap.get("primaryId"));
				command.setCommandDeviceId(deviceId);
				command.setCommandDirection(saveType);
				command.setCommandState(1);
				command.setCommandType("");
				CommandMapper.updateCommandState(command);
				// jedis.hset(generateKey(appId, profileId,
				// deviceId).getBytes(), saveType.getBytes(),
				// SerializeUtil.serialize(queue));

				// AppInfoHandler.getInstance().sendStatusChange(appId,
				// profileId, deviceId, 1);
				//
				// DeviceConfirmHandler.getInstance().saveMessageToConfirm(messageId,
				// appId, deviceId,
				// command.getCommandId(), command.getCommandState());
			} else {
				result = null;
			}
		}

		// 解锁
		CacheHandler.release(generateKey(appId, profileId, deviceId));
		jedisPool.returnResource(jedis);

		return result;
	}

	/**
	 * @Description: 判断packetId是否改变，如果改变则丢弃队列首元素，如果未改变则不丢弃，如果还有暂存数据返回true否则返回false
	 */
	public boolean isPacketIdChanged(String messageId, String appId, String profileId, String deviceId,
			String saveType) {
		Jedis jedis = jedisPool.getResource();
		String result = null;

		// 获取读写锁
		CacheHandler.acquire(generateKey(appId, profileId, deviceId));

		byte[] message = jedis.hget(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes());

		Queue queue = null;
		if (message != null) {
			queue = (LinkedBlockingQueue) SerializeUtil.unserialize(message);
			result = String.valueOf(queue.peek());

			if (result != null && !queue.isEmpty()) {
				Map messageMap = (Map) JSONObject.parse(result, Feature.OrderedField);
				String packetId = (String) messageMap.get("packetId");

				Command command = new Command();
				if (packetId == null) {
					messageMap.put("packetId", messageId);
					DeviceConfirmHandler.getInstance().saveMessageToConfirm(messageId, appId, deviceId,
							command.getCommandId(), command.getCommandState());

					Queue tempQueue = new LinkedBlockingQueue();
					tempQueue.offer(messageMap);

					queue.poll();
					while (!queue.isEmpty()) {
						tempQueue.offer(queue.poll());
					}

					queue = tempQueue;

				} else if (!packetId.equals(messageId)) {
					queue.poll();

					command.setCommandId((int) messageMap.get("primaryId"));
					command.setCommandDeviceId(deviceId);
					command.setCommandDirection(saveType);
					command.setCommandState(2);
					command.setCommandType("");
					CommandMapper.updateCommandState(command);
				}
				jedis.hset(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes(),
						SerializeUtil.serialize(queue));

				AppInfoHandler.getInstance().sendStatusChange(appId, profileId, deviceId, 1);

			}
		} else {
			queue = new LinkedBlockingQueue();
		}

		// 解锁
		CacheHandler.release(generateKey(appId, profileId, deviceId));
		jedisPool.returnResource(jedis);

		return !queue.isEmpty();
	}

	/**
	 * @Description: 是否还有暂存报文
	 */
	public boolean hasNextMessage(String appId, String profileId, String deviceId, String saveType) {
		Jedis jedis = jedisPool.getResource();
		String result = null;

		// 获取读写锁
		CacheHandler.acquire(generateKey(appId, profileId, deviceId));

		byte[] message = jedis.hget(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes());

		if (message != null) {
			Queue queue = (LinkedBlockingQueue) SerializeUtil.unserialize(message);
			queue.poll();

			if (queue.size() == 0) {
				return true;
			}
		}

		// 解锁
		CacheHandler.release(generateKey(appId, profileId, deviceId));
		jedisPool.returnResource(jedis);

		return false;
	}

	/**
	 * @Description: 取暂存数据队列
	 */
	private byte[] retireMessageQueue(String appId, String profileId, String deviceId, String saveType) {
		Jedis jedis = jedisPool.getResource();
		// 获取读写锁
		CacheHandler.acquire(generateKey(appId, profileId, deviceId));

		byte[] message = jedis.hget(generateKey(appId, profileId, deviceId).getBytes(), saveType.getBytes());

		// 解锁
		CacheHandler.release(generateKey(appId, profileId, deviceId));
		jedisPool.returnResource(jedis);

		return message;
	}

	/**
	 * @Description: 更新报文状态
	 */
	public boolean updateMessageStatus(int commandKey, int status) {
		Jedis jedis = jedisPool.getResource();

		Command command = new Command();
		command.setCommandId(commandKey);
		command.setCommandState(status);
		command.setCommandType("");

		jedisPool.returnResource(jedis);
		return CommandMapper.updateCommandState(command) > 0;
	}

	private String generateKey(String appId, String profileId, String deviceId) {
		return appId + "-" + profileId + "-" + deviceId;
	}
}
