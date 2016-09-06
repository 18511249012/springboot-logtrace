package com.hzcard.logtrace.spring.boot.autoconfigure;

import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import com.hzcard.logtrace.event.EventIimmutable;
import com.hzcard.logtrace.event.IEventConstant;
import com.hzcard.logtrace.event.RequestVariablHolder;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, ServletRegistration.class })
@EnableConfigurationProperties(LogTraceProperties.class)
public class LogTraceAutoConfiguration  {


	@Autowired
	private LogTraceProperties properties;

	@Bean
	public feign.Logger.Level feignLoggerLevel() {
		return feign.Logger.Level.FULL;
	}

	@Bean
	public feign.Logger logTraceLogger() {
		return new CustomFeignLogger("feignClient", properties);
	}



	@Bean
	public RequestInterceptor interceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				HystrixRequestVariableDefault<Map<EventIimmutable, Integer>> requestVariable = RequestVariablHolder
						.getInstance();
				Map<EventIimmutable, Integer> event = null;
				try {
					event = requestVariable.get();
				} catch (Throwable e) {
					// logger.error("RequestInterceptor exception ",e);
				}
				if (event != null && event.size() > 0) {
					Set<EventIimmutable> rEvents = event.keySet();
					for (EventIimmutable rEvent : rEvents) {
						if (rEvent.getEventType() != null)
							template.header(IEventConstant.X_EVENT_TYPE, rEvent.getEventType());
						if (rEvent.getEventId() != null)
							template.header(IEventConstant.X_EVENT_ID, rEvent.getEventId());
						if (rEvent.getEventCode() != null)
							template.header(IEventConstant.X_EVENT_CODE, rEvent.getEventCode());
						if (rEvent.getEventPlatform() != null)
							template.header(IEventConstant.X_EVENT_PLATFORM, rEvent.getEventPlatform());
						if (rEvent.getEventSequence() != null){
							if(event.get(rEvent)==null)
								event.put(rEvent, 0);
							else
								event.put(rEvent, event.get(rEvent)+1);
							template.header(IEventConstant.X_EVENT_SEQUENCE, rEvent.getEventSequence()+"-"+event.get(rEvent));
						}
					}
				}
			}
		};
	}


}
