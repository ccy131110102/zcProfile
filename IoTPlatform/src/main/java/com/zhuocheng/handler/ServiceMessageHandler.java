package com.zhuocheng.handler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.util.ParamConverter;

/**
 * @Description: 服务消息处理工具类
 */
public class ServiceMessageHandler {
	private String start = "68";
	private String id = "00";
	private String control;
	private String length;
	private String timestamp;
	private String address;
	private String command;
	private String data;
	private String check;
	private String end = "16";

	private String appId;
	private String profileId;
	private String serviceId;
	private String method;
	private String deviceId;

	private Map profile;
	private Map paras;

	/*
	 * { "deviceId": 18239040000, "maxRetransmit": 3, "command":{ "serviceId":
	 * "error", "method": "alarm", "paras": { "flag" : 540674, "time" :
	 * 1546859285
	 * 
	 * } } }
	 */
	public ServiceMessageHandler(Map serviceMessage) throws ProfileHandleException, ParseException {
		// 1.从服务器信息中获取相应参数
		this.deviceId = (String) serviceMessage.get(Constant.SERVICE_MESSAGE_DEVICEID);
		this.appId = (String) serviceMessage.get(Constant.SERVICE_MESSAGE_APPID);
		this.profileId = (String) serviceMessage.get(Constant.SERVICE_MESSAGE_PROFILEID);
		Map commandMap = (Map) serviceMessage.get(Constant.SERVICE_MESSAGE_COMMAND);
		this.timestamp = Integer.toHexString((int) (System.currentTimeMillis() / 1000)).toUpperCase();

		// 2.获取编码profile
		this.serviceId = (String) commandMap.get(Constant.SERVICE_MESSAGE_COMMAND_SERVICEID);
		this.method = (String) commandMap.get(Constant.SERVICE_MESSAGE_COMMAND_METHOD);
		this.paras = (Map) commandMap.get(Constant.SERVICE_MESSAGE_COMMAND_PARAS);

		// 3.获取设备的注册信息，用于获取控制码
		Map deviceInfoMap = DeviceInfoHandler.getInstance().getDeviceInfoByDeviceKey(deviceId);
		this.control = (String) deviceInfoMap.get(Constant.DEVICEINFO_CONTROL);

		this.profile = ProfileHandler.getInstance().getEncodeProfileByMethod(serviceId, method, profileId, appId);

		// 4.根据profile拼接数据域
		this.data = combileDataByProfile(this.paras);

		System.out.println("数据域 " + this.data);

		this.length = Integer.toHexString(18 + this.data.length() / 2);

		this.deviceId = ((String) serviceMessage.get(Constant.SERVICE_MESSAGE_DEVICEID)).split("-")[2];
		this.address = deviceId;

		this.check = getCheckCode(start + id + control + length + timestamp + address + command + data);

	}

	/**
	 * @Description: 计算校验码
	 */
	public String getCheckCode(String tempStr) {
		String regex = "(.{2})";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(tempStr);

		List<String> tempList = new ArrayList<String>();

		while (m.find()) {
			tempList.add(m.group());
		}

		int sum = 0;

		for (int i = 0; i < tempList.size(); i++) {
			sum = (sum + Integer.parseInt(tempList.get(i), 16));
		}

		// 校验码计算过程:取校验位前的每一位进行算数求和，将得到的结果和FF进行异或运算
		String result = Integer.toHexString(sum ^ 0xFF);

		if (result.length() == 1) {
			result = "0" + result;
		} else {
			result = result.substring(result.length() - 2, result.length());
		}

		return result;
	}

	/**
	 * @Description: 拼接消息体
	 */
	public Map combileMessageMap() {
		Map result = new HashMap();

		result.put("start", start);
		result.put("id", id);
		result.put("control", control);
		result.put("length", length);
		result.put("timestamp", timestamp);
		result.put("address", address);
		result.put("command", command);
		result.put("data", data);
		result.put("check", check);
		result.put("end", end);

		result.put("appId", appId);
		result.put("serviceId", serviceId);
		result.put("method", method);
		result.put("deviceId", deviceId);
		result.put("profileId", profileId);

		result.put("paras", paras);

		return result;
		// return start + id + control + length + timestamp + address + command
		// + data + check + end;
	}

	public static String messageMapToMessage(Map messageMap) {

		String s = (String) messageMap.get("start");
		String i = (String) messageMap.get("id");
		String cl = (String) messageMap.get("control");
		String l = (String) messageMap.get("length");
		String t = (String) messageMap.get("timestamp");
		String a = deviceIdToAddress((String) messageMap.get("address"));
		String cd = (String) messageMap.get("command");
		String d = (String) messageMap.get("data");
		String ck = (String) messageMap.get("check");
		String e = (String) messageMap.get("end");

		return s + i + cl + l + t + a + cd + d + ck + e;
	}

	/**
	 * @Description: 倒转时间戳，倒转后的时间戳用于拼接
	 */
	private String flipTimestamp(String timestamp) {
		String[] timestampArray = timestamp.split(",");
		String newTimestamp = "";
		for (int i = timestampArray.length - 1; i >= 0; i--) {
			newTimestamp = newTimestamp + timestampArray[i] + ",";
		}

		newTimestamp = newTimestamp.substring(0, newTimestamp.length() - 1);
		return newTimestamp;
	}

	/**
	 * @Description: 将设备ID转换为地址域 ：长度为14的16进制字符串
	 */
	private static String deviceIdToAddress(String deviceId) {
		String hexStr = Long.toHexString(Long.valueOf(deviceId));

		while (hexStr.length() < 14) {
			hexStr = "0" + hexStr;
		}

		return hexStr;
	}

	/**
	 * @throws ParseException
	 * @Description: 根据profile对数据域进行编码并返回
	 */
	private String combileDataByProfile(Map paras) throws ParseException {
		// 根据参数列表解析数据域
		// 1.获取profile中的参数列表
		Iterator profileIt = profile.keySet().iterator();
		String profileId = (String) profileIt.next();
		Map profileMap = (Map) profile.get(profileId);
		Iterator serverIt = profileMap.keySet().iterator();
		String serverId = (String) serverIt.next();
		Map propertiesMap = (Map) profileMap.get(serverId);
		Iterator propertiesIt = propertiesMap.keySet().iterator();
		String serivceType = "";
		while (propertiesIt.hasNext()) {
			serivceType = (String) propertiesIt.next();
			if (serivceType.equals(Constant.SERVICE_MESSAGE_COMMAND_PARAS)
					|| serivceType.equals(Constant.PUBLISH_TYPE_PROPERTIES)) {
				break;
			}
		}

		// Map methodMap = (Map) propertiesMap.get(serivceType);
		JSONArray paraList = (JSONArray) propertiesMap.get(serivceType);
		this.command = propertiesMap.get("commandId").toString().replaceAll("0x", "");

		// 2遍历参数列表并拼接数据域
		String dataStr = "";

		System.out.println("参数列表 " + paraList);

		for (Object para : paraList) {
			String paraName = (String) ((Map) para).get(Constant.PARAS_PROPERTYNAME);
			String paraType = (String) ((Map) para).get(Constant.PARAS_DATATYPE);
			int paraLength = (int) ((Map) para).get(Constant.PARAS_LENGTH) * 2;

			String paraData = "";
			if (paras.get(paraName) != null) {
				paraData = String.valueOf(paras.get(paraName));
				paraData = ParamConverter.paramToData(paraType, paraLength, paraData);
			}

			dataStr = dataStr + paraData;
		}

		return dataStr;
	}
}
