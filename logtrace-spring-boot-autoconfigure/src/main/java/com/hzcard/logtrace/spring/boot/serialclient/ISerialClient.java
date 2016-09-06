package com.hzcard.logtrace.spring.boot.serialclient;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(SerialServiceUrlConstant.SERIAL_INSTANCE)
public interface ISerialClient extends SerialServiceUrlConstant{
	String GROUP_KEY = "GROUP_Serial";
	String TIME_OUT="3000";
	
	@RequestMapping(value = SERIAL_SN_GENERATOR, method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String,Object> postGenerateSN(@RequestParam("bizType") String bizType,@RequestParam("code") String code);
	
	@RequestMapping(value = SERIAL_SN_GENERATOR, method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String,Object> getGenerateSN(@RequestParam("bizType") String bizType,@RequestParam("code") String code);
}
