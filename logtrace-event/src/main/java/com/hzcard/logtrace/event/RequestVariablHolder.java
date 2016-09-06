package com.hzcard.logtrace.event;

import java.util.Map;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

public class RequestVariablHolder {

	private final static HystrixRequestVariableDefault<Map<EventIimmutable,Integer>> INSTANCE = new HystrixRequestVariableDefault<Map<EventIimmutable,Integer>>();
	
	public static HystrixRequestVariableDefault<Map<EventIimmutable,Integer>> getInstance(){
		return INSTANCE;
	}
}
