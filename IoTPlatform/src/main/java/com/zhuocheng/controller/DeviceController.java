package com.zhuocheng.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProcessorException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.AppInfoHandler;
import com.zhuocheng.handler.DeviceConfirmHandler;
import com.zhuocheng.handler.DeviceInfoHandler;
import com.zhuocheng.handler.DeviceMessageHandler;
import com.zhuocheng.handler.MessageStorageHandler;
import com.zhuocheng.handler.ProfileHandler;
import com.zhuocheng.handler.ServiceMessageHandler;
import com.zhuocheng.mapper.CommandMapper;
import com.zhuocheng.processor.DecodeProcessor;
import com.zhuocheng.processor.factory.ProcessorFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: 设备控制器，响应设备请求
 */
@Controller
public class DeviceController {
	@Autowired
	private JedisPool jedisPool;
	@Autowired
	private CommandMapper commandMapper;
	@Autowired
	private ServiceController serviceController;
	private Logger logger = Logger.getLogger(DeviceController.class);
	private HashSet<String> deviceIdSet = new HashSet<String>();

	/**
	 * @Description: 用于初始化设备相关处理器
	 */
	@PostConstruct
	public void serviceSubscribe() {
		DeviceInfoHandler.init(jedisPool);
		ProfileHandler.init(jedisPool);
		DeviceConfirmHandler.init(jedisPool, commandMapper);
	}

	/**
	 * @Description: 载入设备ID列表，用于检验设备ID是否存在
	 */
	@PostConstruct
	public void loadDeviceIdList() {
		Jedis jedis = jedisPool.getResource();
		Map deviceMap = jedis.hgetAll("DEVICE");

		Iterator<String> saveIdIt = deviceMap.keySet().iterator();
		while (saveIdIt.hasNext()) {
			String saveId = saveIdIt.next();

			String deviceId = saveId.split("-")[2];
			deviceIdSet.add(deviceId);
		}

		jedisPool.returnResource(jedis);
	}

	/**
	 * @Description: 获取设备列表
	 */
	@RequestMapping(value = "/selectDevice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String selectDevice(@RequestParam("paging") boolean paging, @RequestParam("pageNumber") int pageNumber,
			@RequestParam("pageSize") int pageSize, @RequestBody String content) {
		JSONObject jsonContent = (JSONObject) JSONObject.parse(content, Feature.OrderedField);

		// 用户保存返回结果
		Map resultMap = new HashMap();
		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);

		// 从json结构中获取查询依赖字段
		String appId = jsonContent.getString("appId");
		String profileId = jsonContent.getString("profileId");
		String deviceId = jsonContent.getString("deviceId") == null ? "" : jsonContent.getString("deviceId");
		String deviceInfo = jsonContent.getString("deviceInfo");
		String patten = appId + "-" + profileId + "-" + deviceId;

		if (appId == null || profileId == null) {
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		} else {

			// 获取分页信息
			boolean isPage = paging;
			int startIndex = pageNumber * pageSize;
			int endIndex = startIndex + pageSize;

			// 获取所有的设备信息并根据分页信息进行分页
			Jedis jedis = jedisPool.getResource();
			Map<String, String> deviceMap = jedis.hgetAll("DEVICE");

			Iterator deviceIdIt = deviceMap.keySet().iterator();
			ArrayList<String> deviceIdList = new ArrayList<String>();

			while (deviceIdIt.hasNext()) {
				String dId = (String) deviceIdIt.next();

				if (dId.contains(patten)) {
					if (deviceId != null && !deviceId.equals("") && !dId.equals(patten)) {
						continue;
					}

					if (deviceInfo != null) {
						Map deviceInfoMap = (Map) JSONObject.parse(deviceMap.get(dId), Feature.OrderedField);
						if (deviceInfo.equals(deviceInfoMap.get("deviceInfo"))) {
							deviceIdList.add(dId);
						}
					} else {
						deviceIdList.add(dId);
					}
				}
			}
			int number = deviceIdList.size();

			// 如果不需要分页则重置查询起始和结束序号
			if (!isPage) {
				startIndex = 0;
				endIndex = deviceIdList.size();
			} else {
				if (startIndex >= deviceIdList.size() && number != 0) {
					errorCode = Constant.PAGE_ERRORCODE;
					errorInfo = Constant.PAGE_ERRORMSG;
				}
				if (endIndex >= deviceIdList.size()) {
					endIndex = number;
				}
			}

			// 拼接返回结构体
			JSONArray deviceList = new JSONArray();
			Map bodyMap = new HashMap();
			for (int i = startIndex; i < endIndex; i++) {
				Map deviceInfoMap = (Map) JSONObject.parse(deviceMap.get(deviceIdList.get(i)), Feature.OrderedField);

				Map dMap = new HashMap();
				dMap.put("id", deviceIdList.get(i));
				dMap.put("deviceId", deviceInfoMap.get("deviceId"));
				dMap.put("deviceInfo", deviceInfoMap.get("deviceInfo") == null ? "" : deviceInfoMap.get("deviceInfo"));
				deviceList.add(dMap);
			}

			bodyMap.put("number", number);
			bodyMap.put("selectList", deviceList);
			resultMap.put("body", bodyMap);

			jedisPool.returnResource(jedis);
		}
		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);

		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * @Description: 设备注册
	 * @param deviceInfo
	 *            json格式的键值对，包括deviceId,appId,diviceType
	 */
	@RequestMapping(value = "/deviceRegister", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String deviceRegister(@RequestBody String content) {
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();

		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		String appId = "";
		String profileId = "";
		String deviceId = "";
		String deviceInfo = "";
		String deviceControl = "";
		Map deviceInfoMap = new HashMap();

		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);
		try {
			Map paramsMap = (Map) JSONObject.parse(content, Feature.OrderedField);

			appId = (String) paramsMap.get("appId");
			profileId = (String) paramsMap.get("profileId");
			deviceInfo = (String) paramsMap.get("deviceInfo");
			deviceId = (String) paramsMap.get("deviceId");
			deviceControl = (String) paramsMap.get("control");

			if (appId == null || appId.equals("") || deviceInfo == null || deviceInfo.equals("")
					|| deviceControl == null || deviceControl.equals("")) {
				errorCode = Constant.JSON_ERRORCODE;
				errorInfo = Constant.JSON_ERRORMSG;
			} else {
				deviceInfoMap.put("appId", appId);
				deviceInfoMap.put("profileId", profileId);
				deviceInfoMap.put("deviceInfo", deviceInfo);
				deviceInfoMap.put("deviceId", deviceId);
				deviceInfoMap.put("control", deviceControl);

				if (!AppInfoHandler.getInstance().appIsExist(appId)) {
					errorCode = Constant.APP_OP_ERROR_NOTEXIST_CODE;
					errorInfo = Constant.APP_OP_ERROR_NOTEXIST_MSG;
				} else if (deviceIdSet.contains(deviceId)) {
					errorCode = Constant.DEVICE_OP_ERROR_REGISTER_CODE;
					errorInfo = Constant.DEVICE_OP_ERROR_REGISTER_MSG;
				} else {
					DeviceInfoHandler.getInstance().saveDeviceInfo(deviceId, profileId, appId, deviceInfoMap);
					this.loadDeviceIdList();
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage());
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		}

		resultBodyMap.put("deviceInfo", deviceInfoMap);
		resultBodyMap.put("deviceId", deviceId);

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);
		resultMap.put(Constant.CALLBACK_BODY, resultBodyMap);

		serviceController.serviceSubscribe();
		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * @Description: 移除已注册设备
	 */
	@RequestMapping(value = "/deviceRemove", method = RequestMethod.GET)
	public void deviceRemove(@RequestParam(value = "deviceInfo") String deviceInfo) {

	}

	/**
	 * @Description: 设备数据修改
	 */
	@RequestMapping(value = "/deviceDataChanged", method = RequestMethod.GET)
	public void deviceDataChanged(@RequestParam(value = "deviceInfo") String deviceInfo) {

	}

	/**
	 * @throws IOException
	 * @throws ProfileHandleException
	 * @Description: 设备请求响应
	 */
	@RequestMapping(value = "/communication", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public void deviceResponse(@RequestParam(value = "message") String message, HttpServletResponse response)
			throws IOException {
		response.reset();
		// response.setHeader("Access-Control-Allow-Origin", "*");
		// response.setHeader("Access-Control-Allow-Methods",
		// "PUT,POST,GET,DELETE,OPTIONS");
		// response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		// response.setHeader("Access-Control-Allow-Credentials","true");
		// response.setHeader("Cache-Control", "no-cache");
		// response.setHeader("Content-Type",
		// "text/plain;application/json;charset=UTF-8");
		// response.setHeader("Transfer-Encoding", "chunked");

		// 用于保存返回报文
		String sendStr = "";
		PrintWriter pw = null;

		DecodeProcessor dprocessor;
		String combileMessageStr = null;

		System.out.println(message);

		// 根据当前设备的编号从redis中获取相应的设备及profile信息
		String deviceId = String.valueOf(Long.parseLong(new DeviceMessageHandler(message).getAddress(), 16));
		Map deviceInfoMap = DeviceInfoHandler.getInstance().getDeviceInfoByDeviceId(deviceId);
		String appId = (String) deviceInfoMap.get(Constant.DEVICEINFO_APPID);
		String profileId = (String) deviceInfoMap.get(Constant.DEVICEINFO_PROFILEID);

		String command = new DeviceMessageHandler(message).getCommand();
		String id = new DeviceMessageHandler(message).getId();

		if (DeviceConfirmHandler.getInstance().hasMessageToConfirm(id, appId, deviceId)) {
			DeviceConfirmHandler.getInstance().confirmMessage(id, profileId, appId, deviceId);
		}

		try {
			if (command.equals("00")) {
				// 取暂存指令

				// 判断当前指令的ID是否改变，如果改变则说明上次“取暂存”已完毕，剔除队首消息
				MessageStorageHandler.getInstance().isPacketIdChanged(id, appId, profileId, deviceId,
						Constant.COMMAND_SAVETYPE_DOWN);

				// 获取当前暂存队首消息
				String messageStr = MessageStorageHandler.getInstance().retireMessage(id, appId, profileId, deviceId,
						Constant.COMMAND_SAVETYPE_DOWN);

				// 暂存队列不为空则拼接响应报文体
				if (messageStr != null) {
					Map messageMap = (Map) JSONObject.parse(messageStr, Feature.OrderedField);
					messageMap.put("id", id);
					combileMessageStr = ServiceMessageHandler.messageMapToMessage(messageMap);
					if (combileMessageStr != null) {
						combileMessageStr = combileMessageStr.toUpperCase();
					}
				}

				// 暂存队列为空则返回0000通知设备暂存队列为空停止“取暂存”动作
				if (combileMessageStr == null) {
					combileMessageStr = new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler
							.getInstance().hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN),
							command);
				}

				pw = response.getWriter();
				pw.write(combileMessageStr.toUpperCase());
				response.flushBuffer();
				// return combileMessageStr.toUpperCase();
			} else if (command.equals("FF")) {
				// 如果当前命令字为FF，则直接响应响应确认报文FF
				pw = response.getWriter();
				pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
						.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
						.toUpperCase());
				response.flushBuffer();
			} else {
				dprocessor = ProcessorFactory.createDecodeProcessor(message, jedisPool);
				// 既不是00也不是FF，则为上传动作，将相应上传信息解码并保存回调
				dprocessor.publish(dprocessor.decode(), Constant.PUBLISH_TYPE_PROPERTIES);

				pw = response.getWriter();
				pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
						.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
						.toUpperCase());
				response.flushBuffer();

				// return new
				// DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
				// .hasNextMessage(appId, deviceId,
				// Constant.COMMAND_SAVETYPE_DOWN)).toUpperCase();
			}

		} catch (ProcessorException e) {
			// 写错误日志
			e.printStackTrace();
			logger.error(e.getMessage());
			pw = response.getWriter();
			pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
					.toUpperCase());
			response.flushBuffer();
		} catch (HttpRequestException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			// e.printStackTrace();

			pw = response.getWriter();
			pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
					.toUpperCase());
			response.flushBuffer();

			// return new DeviceMessageHandler(message).combileConfirmMessage(
			// MessageStorageHandler.getInstance().hasNextMessage(appId,
			// deviceId, Constant.COMMAND_SAVETYPE_DOWN))
			// .toUpperCase();

		} catch (ProfileHandleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			pw = response.getWriter();
			pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
					.toUpperCase());
			response.flushBuffer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			pw = response.getWriter();
			pw.write(new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
					.toUpperCase());
			response.flushBuffer();
		}finally {
			System.out.println("close");
			pw.close();
		}

	}

	
}
