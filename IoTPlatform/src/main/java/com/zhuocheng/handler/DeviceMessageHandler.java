package com.zhuocheng.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zhuocheng.constant.Constant;

/**
 * @Description: 设备消息处理工具类
 */
public class DeviceMessageHandler {

	private String start;
	private String id;
	private String control;
	private String length;
	private String timestamp;
	private String address;
	private String command;
	private String data;
	private String check;
	private String end;

	public DeviceMessageHandler(String message) {
		start = message.substring(0, 2);
		id = message.substring(2, 4);
		control = message.substring(4, 6);
		length = message.substring(6, 8);
		timestamp = message.substring(8, 16);
		address = message.substring(16, 30);
		command = message.substring(30, 32);
		data = message.substring(32, message.length() - 4);
		check = message.substring(message.length() - 4, message.length() - 2);
		end = message.substring(message.length() - 2, message.length());

	}

	/**
	 * @Description: 根据规则切分消息体
	 * @param messasge（完整消息体）
	 */
	public Map<String, String> separateMessage() {
		Map<String, String> construction = new HashMap<String, String>();

		// 拼接Map结构体
		construction.put(Constant.CONSTRUCTION_START, start);
		construction.put(Constant.CONSTRUCTION_ID, id);
		construction.put(Constant.CONSTRUCTION_CONTROL, control);
		construction.put(Constant.CONSTRUCTION_LENGTH, length);
		construction.put(Constant.CONSTRUCTION_TIMESTAMP, timestamp);
		construction.put(Constant.CONSTRUCTION_ADDRESS, address);
		construction.put(Constant.CONSTRUCTION_COMMAND, command);
		construction.put(Constant.CONSTRUCTION_DATA, data);
		construction.put(Constant.CONSTRUCTION_CHECK, check);
		construction.put(Constant.CONSTRUCTION_END, end);

		return construction;
	}

	/**
	 * @Description: 解析原始数据并拼接Map
	 */
	public Map combileDecodeOriginalMessage() {
		Map decodeMessage = new HashMap();

		decodeMessage.put("packetId", id);
		decodeMessage.put("deviceId", String.valueOf(Long.parseLong(address, 16)));
		decodeMessage.put("time", timerampToDate(timestamp));
		decodeMessage.put("commandId", command);
		decodeMessage.put("data", data);

		return decodeMessage;
	}
	
	/**
	 * @Description: 生成确认报文
	 */
	public String combileConfirmMessage(boolean hasNextMessage, String command) {
		this.data = "00";
		if(command.equals("00")){
			this.command = "00";
		}else{
			this.command = "FF";
		}
		
		this.timestamp = Integer.toHexString((int) (System.currentTimeMillis() / 1000)).toUpperCase();
		
		if(!hasNextMessage){
			this.control = Integer.toHexString(Integer.parseInt(control, 16) + 128);
		}
		
		this.length = Integer.toHexString(18 + this.data.length() / 2);
		this.check = getCheckCode(start + id + control + length + timestamp + address + this.command + data);
		
		return start + id + control + length + timestamp + address + this.command + data + check + end;
	}

	/**
	 * @Description: 计算校验码
	 */
	public String getCheckCode(String tempStr) {
		String regex = "(.{2})";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(tempStr);
		
		List<String>  tempList =  new ArrayList<String>();
		
		while(m.find()){
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
	 * @Description: 倒置时间戳
	 */
	private String flipTimestamp(String timestamp) {
		String regex = "(.{2})";
		String temp = timestamp.replaceAll(regex, "$1,");
		temp = temp.substring(0, temp.length() - 1);

		String[] timestampArray = temp.split(",");
		String newTimestamp = "";
		for (int i = timestampArray.length - 1; i >= 0; i--) {
			newTimestamp = newTimestamp + timestampArray[i] + ",";
		}

		newTimestamp = newTimestamp.substring(0, newTimestamp.length() - 1);
		return newTimestamp.replace(",", "");
	}

	/**
	 * @Description: 时间戳转换为yyyy-MM-dd HH:mm:ss格式的时间字符串
	 */
	private String timerampToDate(String timgstamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 这个是你要转成后的时间的格式
		return sdf.format(new Date(Long.parseLong(timgstamp, 16) * 1000)); // 时间戳转换成时间
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
	
}
