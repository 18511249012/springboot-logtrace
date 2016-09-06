package com.hzcard.logtrace.spring.boot.handle.autoconfigure;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.hzcard.logtrace.event.EventTypeResolver;
import com.hzcard.logtrace.spring.boot.handle.filter.AaHystrixRequestContextFilter;
import com.hzcard.logtrace.spring.equipment.AndriodEquipMentInterceptor;
import com.hzcard.logtrace.spring.equipment.IPadEquipMentInterceptor;
import com.hzcard.logtrace.spring.equipment.IPhoneEquipMentInterceptor;
import com.hzcard.logtrace.spring.equipment.PcLinuxEquipMentInterceptor;
import com.hzcard.logtrace.spring.equipment.PcMacintoshEquipMentInterceptor;
import com.hzcard.logtrace.spring.equipment.PcWindownEquipMentInterceptor;
@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, ServletRegistration.class})
public class LogTraceHandleAutoConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

	private ApplicationContext context;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new IPhoneEquipMentInterceptor(this.context));
		registry.addInterceptor(new AndriodEquipMentInterceptor(context));
		registry.addInterceptor(new IPadEquipMentInterceptor(this.context));
		registry.addInterceptor(new PcWindownEquipMentInterceptor(context));
		registry.addInterceptor(new PcMacintoshEquipMentInterceptor(context));
		registry.addInterceptor(new PcLinuxEquipMentInterceptor(context));
		
	}
	
	@Bean
	@ConditionalOnMissingBean(EventTypeResolver.class)
	public EventTypeResolver defaultEventTypeResolver(){
		return new EventResolverDefault();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return this.context;
	}
	
	@Bean
	public FilterRegistrationBean hystrixRequestContextFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new AaHystrixRequestContextFilter());
		registration.setOrder(0);
		return registration;
	}
	
	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	
	private static class EventResolverDefault implements EventTypeResolver {

		@Override
		public String eventGen(HttpServletRequest request) {
			return request.getServletPath();
		}

	}


}
