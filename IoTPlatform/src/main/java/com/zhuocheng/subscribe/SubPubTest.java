package com.zhuocheng.subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.handler.HttpRequest;

public class SubPubTest {
	public static void main(String[] args) throws ParseException, HttpRequestException {
//		IPublisher<String> publisher1 = new PublisherImpl<String>("发布者1");
//		ISubcriber<String> subcriber1 = new SubcriberImpl<String>("1", "up", "1", "");
//		ISubcriber<String> subcriber2 = new SubcriberImpl<String>("2", "down", "1", "");
//		subcriber1.subcribe(subscribePublish);
//		subcriber2.subcribe(subscribePublish);
//		publisher1.publish(subscribePublish, "welcome", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to2", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to3", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to4", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "yy", false, "up", "1", "", "");
		
		
		
		HttpRequest r = new HttpRequest();
		
		
		System.out.println(getCheckCode("688140135C99F5D900000000000001FF00"));
		
//		r.sendPost("http://58.59.64.11:8078/deviceRegister", "");
	}
	
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
			System.out.println(Integer.parseInt(tempList.get(i), 16));
			sum = (sum + Integer.parseInt(tempList.get(i), 16));
			
		}

		System.out.println(sum);
		// 校验码计算过程:取校验位前的每一位进行算数求和，将得到的结果和FF进行异或运算
		sum = sum & 0xFF;
		sum = sum ^ 0xFF;
		String result = Integer.toHexString(sum);

		System.out.println(result);
		
		if (result.length() == 1) {
			result = "0" + result;
		} else {
			result = result.substring(result.length() - 2, result.length());
		}

		return result;
	}
}
