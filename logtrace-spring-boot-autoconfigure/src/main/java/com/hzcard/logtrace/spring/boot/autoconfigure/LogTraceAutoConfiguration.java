package com.hzcard.logtrace.spring.boot.autoconfigure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import com.hzcard.logtrace.event.EventIimmutable;
import com.hzcard.logtrace.event.IEventConstant;
import com.hzcard.logtrace.event.RequestVariablHolder;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import com.netflix.hystrix.util.PlatformSpecific;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, ServletRegistration.class })
@EnableConfigurationProperties(LogTraceProperties.class)
public class LogTraceAutoConfiguration  implements ApplicationContextAware{
	
	private ApplicationContext context;

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
	public HystrixConcurrencyStrategy getConcurrencyStrategy(){
		customHystrixConcurrencyStrategy cust = new customHystrixConcurrencyStrategy();
		HystrixPlugins.getInstance().registerConcurrencyStrategy(new customHystrixConcurrencyStrategy());
		return cust;
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
				}else{	//not Controller start 
					if (!HystrixRequestContext.isCurrentThreadInitialized())
						HystrixRequestContext.initializeContext();
					HystrixRequestVariableDefault<Map<EventIimmutable, Integer>> requestV = RequestVariablHolder.getInstance();
					EventIimmutable ievent = new EventIimmutable("unknow", "unknow", UUID.randomUUID().toString().replace("-", ""), "unknow", "1");
					Map<EventIimmutable, Integer> eventMap = new HashMap<EventIimmutable, Integer>();
					eventMap.put(ievent, 0);
					requestV.set(eventMap);
					template.header(IEventConstant.X_EVENT_ID, ievent.getEventId());
					template.header(IEventConstant.X_EVENT_TYPE, ievent.getEventType());
					template.header(IEventConstant.X_EVENT_CODE, ievent.getEventCode());
					template.header(IEventConstant.X_EVENT_PLATFORM, ievent.getEventPlatform());
					template.header(IEventConstant.X_EVENT_SEQUENCE,ievent.getEventSequence()+"-"+eventMap.get(ievent));
				}
			}
		};
	}
	
	/**
	 * 覆盖HystrixConcurrencyStrategy，以用以区分线程
	 * @author zhangwei
	 *
	 */
	private class customHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy{
		
		@Override
		public ThreadPoolExecutor getThreadPool(final HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
	        ThreadFactory threadFactory = null;
	        if (!PlatformSpecific.isAppEngine()) {
	            threadFactory = new ThreadFactory() {
	                protected final AtomicInteger threadNumber = new AtomicInteger(0);
	                @Override
	                public Thread newThread(Runnable r) {
	                    Thread thread = new Thread(r, "hystrix-" + threadPoolKey.name() + "-"+context.getId()+"-" + threadNumber.incrementAndGet());
	                    thread.setDaemon(true);
	                    return thread;
	                }
	            };
	        } else {
	            threadFactory = PlatformSpecific.getAppEngineThreadFactory();
	        }
	        return new ThreadPoolExecutor(corePoolSize.get(), maximumPoolSize.get(), keepAliveTime.get(), unit, workQueue, threadFactory);
	    }
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
		
	}


}
