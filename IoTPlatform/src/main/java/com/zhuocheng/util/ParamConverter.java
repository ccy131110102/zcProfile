package com.zhuocheng.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zhuocheng.constant.Constant;

/**
 * @Description: 数据转换工具，用于报文与参数列表之间的转换
 */
public class ParamConverter {
	/**
	 * @Description: 参数列表转报文数据
	 */
	public static String paramToData(String paramType, int paramLength, String param) throws ParseException {
		String result = "";

		switch (paramType) {
		case Constant.PARAMTYPE_INT:
			// int类型为带正负的整数类型，高1位为判断位
			long temp = Long.valueOf(param);
			if(temp >= 0){
				result = Long.toHexString(Long.valueOf(param));
			}else{
				result = Long.toHexString(Long.valueOf(param) + (long)Math.pow(2, 8*paramLength/2-1));
			}
			
			break;
		case Constant.PARAMTYPE_UINT32:
			// unit为非负整数，直接转16进制
			result = Long.toHexString(Long.valueOf(param));

			break;
		case Constant.PARAMTYPE_STRING:
			// 字符串转换时低位补0
			char[] charArray = param.toCharArray();
			result = "";

			for (char c : charArray) {
				result = result + Long.toHexString((int) c);
			}

			result = supplyLow(paramLength, result);

			break;
//		case Constant.PARAMTYPE_UINT16:
//			result = Long.toHexString(Long.valueOf(param));
//
//			break;
//		case Constant.PARAMTYPE_UINT8:
//			result = Long.toHexString(Long.valueOf(param));
//
//			break;
		case Constant.PARAMTYPE_TIMESTAMP:
			// 时间戳，入参格式为yyyy-MM-dd HH:mm:ss，转换为对应时间戳
			result = dateToTimeramp(param);

			break;
		case Constant.PARAMTYPE_BOOL:
			// 布尔类型，true为1，false为0
			if (param.equals("true")) {
				result = "1";
			} else {
				result = "0";
			}

			break;
		default:
			break;
		}

		result = supplyHigh(paramLength, result);
		return result;
	}

	/**
	 * @Description: 报文数据转参数列表
	 */
	public static Object dataToParam(String paramType, int paramLength, String param) throws ParseException {
		Object result = "";

		String flipParam = param;

		switch (paramType) {
		case Constant.PARAMTYPE_INT:
			// int带正负的整数类型，高1位为正负判断位
			result = Long.parseLong(flipParam, 16);
			double boundary = Math.pow(2, 8*paramLength/2-1);
			
			if((long)result >= boundary){
				result = (long)result - (long)boundary * 2;
			}
			
			break;
		case Constant.PARAMTYPE_UINT32:
			// 非负整数，转十进制
			result = Long.parseLong(flipParam, 16);

			break;
		case Constant.PARAMTYPE_STRING:
			// 00为结束符
			String regex = "(.{2})";
			String temp = param.replaceAll(regex, "$1,");
			temp = temp.substring(0, temp.length() - 1);

			String[] strArray = temp.split(",");

			for (String str : strArray) {
				if (str != "00") {
					result = result + String.valueOf((char) (Integer.parseInt(str, 16)));
				}
			}

			break;
//		case Constant.PARAMTYPE_UINT16:
//			result = Long.parseLong(flipParam, 16);
//
//			break;
//		case Constant.PARAMTYPE_UINT8:
//			result = Long.parseLong(flipParam, 16);
//
//			break;
		case Constant.PARAMTYPE_TIMESTAMP:
			// 时间戳转换为时间，格式为yyyy-MM-dd HH:mm:ss
			result = timestampToDate(flipParam);

			break;
		case Constant.PARAMTYPE_BOOL:
			// 布尔类型 01位true，00位false
			if (flipParam.equals("01")) {
				result = true;
			} else {
				result = false;
			}

			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * @Description: 高位补0
	 */
	private static String supplyHigh(int lenghth, String str) {
		String temp = str;

		while (temp.length() > lenghth) {
			temp.substring(0, 1);
		}

		while (temp.length() < lenghth) {
			temp = "0" + temp;
		}

		return temp;
	}

	/**
	 * @Description: 低位补0
	 */
	private static String supplyLow(int lenghth, String str) {
		String temp = str;

		while (temp.length() > lenghth) {
			temp.substring(temp.length() - 1, temp.length());
		}

		while (temp.length() < lenghth) {
			temp = temp + "0";
		}

		return temp;
	}

	/**
	 * @Description: 倒置参数
	 */
	private static String flipParam(String param) {
		String regex = "(.{2})";
		String temp = param.replaceAll(regex, "$1,");
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
	private static String timestampToDate(String timgstamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 这个是你要转成后的时间的格式
		return sdf.format(new Date(Long.parseLong(timgstamp, 16) * 1000)); // 时间戳转换成时间
	}

	/**
	 * @throws ParseException
	 * @Description: yyyy-MM-dd HH:mm:ss格式的时间字符串转换为时间戳
	 */
	private static String dateToTimeramp(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 这个是你要转成后的时间的格式
		Date d = sdf.parse(date);
		return Long.toHexString(d.getTime()/1000).toUpperCase(); // 时间戳转换成时间
	}
}
