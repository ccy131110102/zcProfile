package com.zhuocheng.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Description: 响应体增强器，用于向响应体增添头信息以支持跨域访问
 */
@ControllerAdvice
public class CORSResponseBodyAdvice implements ResponseBodyAdvice{

    public Object beforeBodyWrite(Object returnValue, MethodParameter methodParameter,
            MediaType mediaType, Class clas, ServerHttpRequest serverHttpRequest,
            ServerHttpResponse serverHttpResponse) {
    	// 新增头信息用于前端跨域访问
    	serverHttpResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
    	serverHttpResponse.getHeaders().set("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
    	serverHttpResponse.getHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    	serverHttpResponse.getHeaders().set("Access-Control-Allow-Credentials","true");
    	return returnValue;
    }

    public boolean supports(MethodParameter methodParameter, Class clas) {
        return true;
    }
}
