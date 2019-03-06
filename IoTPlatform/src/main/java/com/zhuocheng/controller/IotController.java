package com.zhuocheng.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.zhuocheng.mapper.CommandMapper;
import com.zhuocheng.model.Command;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description: iot控制器，响应设备请求
 */
@Controller
public class IotController {

	@Autowired
	JedisPool jedisPool;

	private String profileStr;

	@Autowired
	private CommandMapper commandMapper;

	/**
	 * @Description: 导入profile结构体
	 */
	// @RequestMapping(value = "/profileRegister", method = RequestMethod.POST)
	// public void profileRegister(@RequestParam(value = "construction") String
	// construction, @RequestParam(value = "profileId") String profileId){
	// Jedis je = jedisPool.getResource();
	//
	// je.hset("PROFILE", profileId, construction);

	// je.close();

	// System.out.println(JSONObject.toJSONString(jo.get("methods")));
	// }

	/**
	 * @Description: 导入app与profile关系结构体
	 */
	// @RequestMapping(value = "/appProfileRegister", method =
	// RequestMethod.GET)
	// public void appProfileRegister(@RequestParam(value = "construction")
	// String construction, @RequestParam(value = "profileId") String
	// profileId){
	// Jedis je = jedisPool.getResource();
	//
	// je.set(Constant.APP_SAVEKEY, construction);
	//
	// je.close();

	// System.out.println(JSONObject.toJSONString(jo.get("methods")));
	// }

	/**
	 * @Description: 根据访问的uri查询上传或下发信息
	 */
	@RequestMapping(value = { "/selectCommand",
			"/selectProperty" }, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String selectCommand(HttpServletRequest request,@RequestParam("paging") boolean paging, @RequestParam("pageNumber") int pageNumber,
			@RequestParam("pageSize") int pageSize, @RequestBody String content) {
		JSONObject jsonContent = (JSONObject) JSONObject.parse(content, Feature.OrderedField);

		String url = request.getRequestURI();
		
		// 用户保存返回结果
		Map resultMap = new HashMap();
		Map resultBodyMap = new HashMap();
		int errorCode = 0;
		String errorInfo = "";
		String requestContent = content;
		resultMap.put(Constant.CALLBACK_REQUESTCONTENT, requestContent);

		// 从json结构中获取查询依赖字段
		String appId = jsonContent.getString("appId");
		String profileId = jsonContent.getString("profileId");
		String deviceId = jsonContent.getString("deviceId");
		String serviceId = jsonContent.getString("serviceId");
		String serviceType = null;
		String direction = null;

		if (appId == null || profileId == null) {
			errorCode = Constant.JSON_ERRORCODE;
			errorInfo = Constant.JSON_ERRORMSG;
		} else {
			// 根据uri获取请求参数
			if (url.equals("/selectCommand")) {
				serviceType = jsonContent.getString("method");
				direction = Constant.COMMAND_SAVETYPE_DOWN;
			}
			if (url.equals("/selectProperty")) {
				serviceType = jsonContent.getString("serviceType");
				direction = Constant.COMMAND_SAVETYPE_UP;
			}

			JSONArray timeArray = jsonContent.getJSONArray("timeRange");
			boolean isPage = paging;
			int startIndex = pageNumber * pageSize;
			int size = pageSize;
			Date startDate = null;
			Date endDate = null;

			// 判断时间列表内时间的先后
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (timeArray == null) {
				startDate = null;
				endDate = null;
			} else {
				String tempTime1 = timeArray.getString(0);
				String tempTime2 = timeArray.getString(1);

				Date tempDate1 = null;
				Date tempDate2 = null;
				try {
					tempDate1 = sdf.parse(tempTime1);
					tempDate2 = sdf.parse(tempTime2);

					if (tempDate1.before(tempDate2)) {
						startDate = tempDate1;
						endDate = tempDate2;
					} else {
						startDate = tempDate2;
						endDate = tempDate1;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 根据已有参数从数据库查询命令列表
			ArrayList<Command> resultList = commandMapper.selectCommand(direction, deviceId, appId, profileId, serviceId,
					serviceType, startDate, endDate, isPage, startIndex, size);
			int number = commandMapper.selectCommandCount(direction, deviceId, appId, profileId, serviceId, serviceType,
					startDate, endDate);

			// 检测分页信息是否正确
			if (pageNumber * pageSize >= number && number != 0) {
				errorCode = Constant.PAGE_ERRORCODE;
				errorInfo = Constant.PAGE_ERRORMSG;
			}

			// 拼接响应结构体
			JSONArray commandJArray = new JSONArray();
			for (Command command : resultList) {
				Map commandMap = new HashMap<>();

				commandMap.put("id", command.getCommandId());
				commandMap.put("serviceiId", command.getCommandServiceId());
				commandMap.put("mehod", command.getCommandServiceType());
				commandMap.put("commandStatus", command.getCommandState());
				commandMap.put("time", sdf.format(command.getCommandTimestamp()));
				if (direction.equals(Constant.COMMAND_SAVETYPE_DOWN)) {
					commandMap.put("paras", ((Map) JSONObject.parse(command.getCommandContent(), Feature.OrderedField)).get("paras"));
				} else {
					Map serviceMap = (Map) JSONObject.parse(
							String.valueOf(((Map) JSONObject.parse(command.getCommandContent(), Feature.OrderedField)).get("service")), Feature.OrderedField);
					commandMap.put("paras", serviceMap.get("data"));
				}
				commandJArray.add(commandMap);
			}

			Map bodyMap = new HashMap();
			bodyMap.put("number", number);
			bodyMap.put("selectList", commandJArray);

			resultMap.put("body", bodyMap);
		}

		resultMap.put(Constant.CALLBACK_ERRORCODE, errorCode);
		resultMap.put(Constant.CALLBACK_ERRORINFO, errorInfo);

		return JSONObject.toJSONString(resultMap);
	}

	@RequestMapping(value = "/test/construction", method = RequestMethod.GET)
	public void test() {
		
	}
}
