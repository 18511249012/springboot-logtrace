package com.hzcard.logtrace.spring.boot.handle.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.hzcard.logtrace.event.EventIimmutable;
import com.hzcard.logtrace.event.EventTypeResolver;
import com.hzcard.logtrace.event.IEventConstant;
import com.hzcard.logtrace.event.RequestVariablHolder;
import com.hzcard.logtrace.spring.boot.handle.LogConstant;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

public abstract class EventHandlerInterceptor implements HandlerInterceptor, LogConstant {

	private EventTypeResolver eventResolver;

	private ApplicationContext applicationContext;

	private static final Logger logger = LoggerFactory.getLogger(EventHandlerInterceptor.class);

	public EventHandlerInterceptor(ApplicationContext context) {
		applicationContext = context;
	}

	public abstract EquipmentTypeEnum resolve(HttpServletRequest request);

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (!HystrixRequestContext.isCurrentThreadInitialized())
			HystrixRequestContext.initializeContext();

		HystrixRequestVariableDefault<Map<EventIimmutable, Integer>> requestV = RequestVariablHolder.getInstance();

		if (request.getServletPath().equals("/"))
			return true;
		EventIimmutable ievent = null;
		String platform = request.getHeader(IEventConstant.X_EVENT_PLATFORM);
		if (request.getHeader(IEventConstant.X_EVENT_ID) != null) {
			ievent = new EventIimmutable(request.getHeader(IEventConstant.X_EVENT_PLATFORM),
					request.getHeader(IEventConstant.X_EVENT_TYPE), request.getHeader(IEventConstant.X_EVENT_ID),
					request.getHeader(IEventConstant.X_EVENT_CODE), request.getHeader(IEventConstant.X_EVENT_SEQUENCE));
			Map<EventIimmutable, Integer> eventMap = new HashMap<EventIimmutable, Integer>();
			eventMap.put(ievent, null);
			requestV.set(eventMap);
		} else {

			EquipmentTypeEnum eqType = resolve(request);
			if (eqType.equals(EquipmentTypeEnum.NOTIDENTITY)) { // 没有识别设备
				return true;
			}
			if (platform != null && platform.trim().length() > 0)
				platform = eqType.toString() + SPLITOSANDPLATFORM + platform;  //各平台会设置platform
			else	//默认是cms
				platform = eqType.toString() + "-" + IEventConstant.WEB_SITE_PLATFORM;
			
			eventResolver = applicationContext.getBean(EventTypeResolver.class);
			String eventType = eventResolver.eventGen(request);
			String eventCode = "";
			if (platform != null) {
				try {
					RestTemplate restTemplate = applicationContext.getBean(RestTemplate.class);
					if (restTemplate != null) {
						MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
						parts.add("bizType", "insertLog");
						parts.add("code", platform);
						@SuppressWarnings("unchecked")
						Map<String, Object> sn = restTemplate
								.postForObject("http://hzcard-serial-service/v1.0/serial/sn", parts, Map.class);
						if (sn != null && !sn.isEmpty()) {
							Object SNCODE = sn.get("sn");
							if (SNCODE != null)
								eventCode = String.valueOf(SNCODE);
						}
					}
				} catch (Throwable th) {
					logger.error("操作流水号获取失败！", th);
				}
			}
			ievent = new EventIimmutable(platform, eventType, UUID.randomUUID().toString(), eventCode, "1");
			Map<EventIimmutable, Integer> eventMap = new HashMap<EventIimmutable, Integer>();
			eventMap.put(ievent, null);
			requestV.set(eventMap);
		}
		if (platform != null)
			response.setHeader(IEventConstant.X_EVENT_PLATFORM, platform);
		response.setHeader(IEventConstant.X_EVENT_TYPE, ievent.getEventType());
		response.setHeader(IEventConstant.X_EVENT_ID, ievent.getEventId());
		response.setHeader(IEventConstant.X_EVENT_CODE, ievent.getEventCode());
		response.addHeader(IEventConstant.X_EVENT_SEQUENCE, ievent.getEventSequence());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		if (HystrixRequestContext.getContextForCurrentThread() != null) {
			HystrixRequestContext.getContextForCurrentThread().shutdown();
		}

	}

}
