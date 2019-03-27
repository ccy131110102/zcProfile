package com.zhuocheng.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 序列化工具，序列化后的类便于存储
 */
public class SerializeUtil {
	/**
	 * @Description: 序列化,用于将对象序列化后存于redis中
	 */
	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * @Description: 反序列化
	 */
	public static Object unserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {

		}
		return null;
	}
	
	/**
	 * @Description: 计算校验码
	 */
	public static String getCheckCode(String tempStr) {
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
}