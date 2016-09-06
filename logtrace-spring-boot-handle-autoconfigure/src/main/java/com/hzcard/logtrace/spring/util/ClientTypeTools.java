package com.hzcard.logtrace.spring.util;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hzcard.logtrace.event.EventIimmutable;
import com.hzcard.logtrace.event.RequestVariablHolder;
import com.hzcard.logtrace.spring.boot.handle.LogConstant;
import com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

public class ClientTypeTools implements LogConstant{
	private static final Logger logger = LoggerFactory.getLogger(ClientTypeTools.class);
	
	public static Integer getClientType(){
		
		HystrixRequestVariableDefault<Map<EventIimmutable, Integer>> requestVariable = RequestVariablHolder
				.getInstance();
		Map<EventIimmutable, Integer> event = null;
		try {
			event = requestVariable.get();
		} catch (Throwable e) {
			// logger.error("RequestInterceptor exception ",e);
		}
		if(event==null)
			return null;
		if (event != null && event.size() > 0) {
			Set<EventIimmutable> rEvents = event.keySet();
			for (EventIimmutable rEvent : rEvents) {
				String eventPlatform = rEvent.getEventPlatform();
				try{
				EquipmentTypeEnum typeEnum = EquipmentTypeEnum.valueOf(eventPlatform.split(SPLITOSANDPLATFORM)[0]);
				return typeEnum.getClientType();
				}catch(Throwable th){
					logger.error(th.getMessage());
				}
			}
		}
		return null;
	}

}
