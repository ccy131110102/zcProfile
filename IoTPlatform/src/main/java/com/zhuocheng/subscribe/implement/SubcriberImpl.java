package com.zhuocheng.subscribe.implement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.AppInfoHandler;
import com.zhuocheng.handler.HttpRequest;
import com.zhuocheng.handler.MessageStorageHandler;
import com.zhuocheng.handler.ProfileHandler;
import com.zhuocheng.subscribe.SubscribePublish;
import com.zhuocheng.subscribe.Interface.ISubcriber;

/**
 * @Description: 订阅者实现类
 */
public class SubcriberImpl<M> implements ISubcriber<M> {
	// 对应commandId,用于判断
	private String subcriberId;
	private String subcriberType;
	private String profileId;
	private String type;

	public SubcriberImpl(String profileId, String subcriberType, String subcriberId, String type) {
		super();
		this.subcriberId = subcriberId;
		this.subcriberType = subcriberType;
		this.profileId = profileId;
		this.type = type;
	}

	public void subcribe(SubscribePublish subscribePublish) {
		subscribePublish.subcribe(this);
	}

	public void unSubcribe(SubscribePublish subscribePublish) {
		subscribePublish.unSubcribe(this);
	}

	public String update(String profileId, M message, String subcriberType, String subcriberId, String type,
			String serviceId, String commandType) throws HttpRequestException, ProfileHandleException {
		String saveId = "";

		if (this.profileId.equals(profileId) && this.subcriberType.equals(subcriberType)
				&& this.subcriberId.equals(subcriberId) && this.type.equals(type)) {
			// 数据上传
			// if (type.equals(Constant.PUBLISH_TYPE_METHOD)) {

			System.out.println("subcriberId:" + this.subcriberId + " subcriberType:" + subcriberType + "发来的消息:" + message.toString());
			// 1.根据profileId获取callbackurl
			String callbackUrl = "";

			// 2.向callbackurl转发解码后的数据结构体
			HttpRequest httpRequest = new HttpRequest();

			// 2.1拼接请求url及参数
			String param = "";
			Map paramMap;
			paramMap = (Map) ((Map) message).get("service");

			String deviceId = String.valueOf(((Map) message).get("deviceId"));

			Map dataMap;

			if (!type.equals(Constant.PUBLISH_TYPE_METHOD)) {
				dataMap = (Map) paramMap.get("data");

				Iterator paramIt = dataMap.keySet().iterator();
				while (paramIt.hasNext()) {
					String paramName = (String) paramIt.next();
					String paramValue = (String) dataMap.get(paramName);

					if (param.equals("")) {
						param = param + paramName + "=" + paramValue;
					} else {
						param = param + "&" + paramName + "=" + paramValue;
					}
				}

				// 2.2 转发请求并接收返回数据
				try {
					String appId = (String) paramMap.get("appId");
					Map sendMap = new LinkedHashMap();
					sendMap.put("target", "propertyUpload");
					sendMap.put("service", paramMap);
					sendMap.put("profileId", profileId);
					sendMap.put("appId", appId);
					sendMap.put("deviceId", deviceId);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 这个是你要转成后的时间的格式
					sendMap.put("receiveTime", sdf.format(new Date()));
					sendMap.put("commandStatus", "0");

					if (!type.equals(Constant.PUBLISH_TYPE_PROPERTIES)) {
						callbackUrl = Constant.LOCAL_STORAGESERVICE_URL;
					} else {
						if (AppInfoHandler.getInstance().getCallBackURL(appId, profileId) == null) {
							callbackUrl = Constant.LOCAL_STORAGESERVICE_URL;
						} else {

							callbackUrl = (String) AppInfoHandler.getInstance().getCallBackURL(appId, profileId)
									.get(Constant.CALLBACK_PROPERTYUPLOAD);
						}
					}
					
					saveId = MessageStorageHandler.getInstance().saveMessage(JSONObject.toJSONString(message),
							ProfileHandler.getInstance().getAppIdByProfileId(profileId), profileId, deviceId,
							Constant.COMMAND_SAVETYPE_UP, "0", subcriberId, subcriberType, commandType);
					sendMap.put("commandKey", saveId);
					
					String responseStr = httpRequest.sendPost(callbackUrl, JSONObject.toJSONString(sendMap));
					MessageStorageHandler.getInstance().updateMessageStatus(Integer.valueOf(saveId), 1);
					
				} catch (HttpRequestException e) {
					// TODO: handle exception
//					saveId = MessageStorageHandler.getInstance().saveMessage(JSONObject.toJSONString(message),
//							ProfileHandler.getInstance().getAppIdByProfileId(profileId), profileId, deviceId,
//							Constant.COMMAND_SAVETYPE_UP, "0", subcriberId, subcriberType, commandType);

					// 重新尝试发送，目前暂时不做重发

					throw new HttpRequestException(e.getMessage(), e.getErrorCode());
				}

			} else {
				dataMap = (Map) message;
				saveId = MessageStorageHandler.getInstance().saveMessage(JSONObject.toJSONString(message),
						ProfileHandler.getInstance().getAppIdByProfileId(profileId), profileId, deviceId,
						Constant.COMMAND_SAVETYPE_DOWN, "0", subcriberId, subcriberType, commandType);
			}

			// }
			// 数据下发
			// else if(type.equals(Constant.PUBLISH_TYPE_PROPERTIES)){
			// String deviceId = String.valueOf(((Map)
			// message).get("deviceId"));
			// String appId = String.valueOf(((Map) message).get("appId"));
			// MessageStorageHandler.getInstance().saveMessage(ServiceMessageHandler.messageMapToMessage((Map)message),
			// appId, deviceId, Constant.COMMAND_SAVETYPE_DOWN);
			// }
		}

		return saveId;
	}

	public String getSubcriberId() {
		return subcriberId;
	}

	public void setSubcriberId(String subcriberId) {
		this.subcriberId = subcriberId;
	}

	public String getSubcriberType() {
		return subcriberType;
	}

	public void setSubcriberType(String subcriberType) {
		this.subcriberType = subcriberType;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}