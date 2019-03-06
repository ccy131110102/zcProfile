package com.zhuocheng.constant;

/**
 * @Description: 用于保存和获取静态字段
 */
public class Constant {
	
	public static final String COMMAND_RESET = "1F"; 
	
	public static final String CONSTRUCTION_START = "START"; 
	public static final String CONSTRUCTION_ID = "ID";
	public static final String CONSTRUCTION_CONTROL = "CONTROL";
	public static final String CONSTRUCTION_LENGTH = "LENGTH";
	public static final String CONSTRUCTION_TIMESTAMP = "TIMESTAMP";
	public static final String CONSTRUCTION_ADDRESS = "ADDRESS";
	public static final String CONSTRUCTION_COMMAND = "COMMAND";
	public static final String CONSTRUCTION_DATA = "DATA";
	public static final String CONSTRUCTION_CHECK = "CHECK";
	public static final String CONSTRUCTION_END = "END";
	
	public static final String CONSTRUCTION_OPLEVEL_SERVER = "SERVER";
	public static final String CONSTRUCTION_OPLEVEL_METHODS = "METHODS";
	public static final String CONSTRUCTION_OPLEVEL_PROPERTIES = "PROPERTIES";
	
	public static final String CONSTRUCTION_SAVEKEY = "PROFILE";
	public static final String CONSTRUCTION_SERVICEKEY = "service";
	public static final String CONSTRUCTION_DATAKEY = "data";
	public static final String CONSTRUCTION_MESSAGEIDKEY = "commandId";
	
	public static final String APP_SAVEKEY = "APP";
	public static final String SERVERS_SAVEKEY = "SERVERS";
	public static final String SERVERS_CALLBACK = "callBackURL";
	
	public static final String COMMAND_SAVETYPE_UP = "up";
	public static final String COMMAND_SAVETYPE_DOWN = "down";
	
	public static final String PARAS_PARASKEY = "paras";
	public static final String PARAS_PROPERTYNAME = "name";
	public static final String PARAS_DATATYPE = "dataType";
	public static final String PARAS_LENGTH = "length";
	
	public static final String PROPERTIES_PARASKEY = "paras";
	public static final String PROPERTIES_PROPERTYNAME = "name";
	public static final String PROPERTIES_DATATYPE = "dataType";
	public static final String PROPERTIES_LENGTH = "length";
	public static final String PROPERTIES_COMMAND = "commandId";
	
	public static final String PROFILE_ID = "profileId";
	public static final String SERVICE_ID = "serviceId";
	public static final String SERVICE_TYPE = "serivceType";
	public static final String SERVICE_DATA = "data";
	public static final String SERVICE_COMMAND_TYPR = "commandType";
	
	public static final String PUBLISH_TYPE_METHOD = "methods";
	public static final String PUBLISH_TYPE_PROPERTIES = "properties";
	
	public static final String DEVICEINFO_DEVICEID = "deviceId";
	public static final String DEVICEINFO_APPID = "appId";
	public static final String DEVICEINFO_PROFILEID = "profileId";
	public static final String DEVICEINFO_CONTROL = "control";
	
	public static final String SERVICE_MESSAGE_APPID = "appId";
	public static final String SERVICE_MESSAGE_DEVICEID = "deviceId";
	public static final String SERVICE_MESSAGE_PROFILEID = "profileId";
	public static final String SERVICE_MESSAGE_COMMAND = "command";
	public static final String SERVICE_MESSAGE_COMMAND_SERVICEID = "serviceId";
	public static final String SERVICE_MESSAGE_COMMAND_METHOD = "method";
	public static final String SERVICE_MESSAGE_COMMAND_PARAS = "paras";
	
	public static final String MESSAGECODE_RETIRE = "0x00";
	
	public static final String PARAMTYPE_UINT32 = "uint";
	public static final String PARAMTYPE_INT = "int";
	public static final String PARAMTYPE_STRING = "string";
	public static final String PARAMTYPE_UINT16 = "uint16";
	public static final String PARAMTYPE_UINT8 = "uint8";
	public static final String PARAMTYPE_TIMESTAMP = "timeStamp";
	public static final String PARAMTYPE_BOOL = "bool";
	
	public static final String CALLBACK_REQUESTCONTENT = "requestContent";
	public static final String CALLBACK_BODY = "body";
	public static final String CALLBACK_ERRORCODE = "errorCode";
	public static final String CALLBACK_ERRORINFO = "errorInfo";
	
	public static final String CALLBACK_PROPERTYUPLOAD = "propertyUpload";
	public static final String CALLBACK_ISSUECOMMANDSTATUSCHANGE = "issueCommandStatusChange";
	
	public static final int CONSTRUCTION_OP_ERROR_NOPROFILE_CODE = 10;
	public static final String CONSTRUCTION_OP_ERROR_NOPROFILE_MSG = "未获取到任何profile结构";
	
	public static final int CONSTRUCTION_OP_ERROR_DECODENOPROFILE_CODE = 11;
	public static final String CONSTRUCTION_OP_ERROR_DECODENOPROFILE_MSG = "profile解码时异常";

	public static final int CONSTRUCTION_OP_ERROR_HTTPREQUEST_CODE = 20;
	public static final String CONSTRUCTION_OP_ERROR_HTTPREQUEST_MSG = "Http请求异常";

	public static final int APP_OP_ERROR_REGISTER_CODE = 30;
	public static final String APP_OP_ERROR_REGISTER_MSG = "当前app名称已存在";
	
	public static final int APP_OP_ERROR_NOTEXIST_CODE = 31;
	public static final String APP_OP_ERROR_NOTEXIST_MSG = "当前app不存在";
	
	public static final int JSON_ERRORCODE = 40;
	public static final String JSON_ERRORMSG = "JSON格式错误或参数格式错误";
	
	public static final int PAGE_ERRORCODE = 60;
	public static final String PAGE_ERRORMSG = "分页信息异常";
	
	public static final int DEVICE_OP_ERROR_REGISTER_CODE = 50;
	public static final String DEVICE_OP_ERROR_REGISTER_MSG = "当前设备编号已存在";
	
//	public static final String LOCAL_STORAGESERVICE_URL = "http://localhost:8080/IoTPlatform";
	public static final String LOCAL_STORAGESERVICE_URL = "http://zc.bloath.com/callback";
	
}
