package com.hzcard.logtrace.spring.boot.autoconfigure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.hzcard.logtrace.HzcardKafkaAppender;
import com.hzcard.logtrace.event.EventIimmutable;
import com.hzcard.logtrace.event.IEventConstant;
import com.hzcard.logtrace.event.RequestVariablHolder;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import feign.Request;
import feign.Response;

public class CustomFeignLogger extends feign.Logger {

	private final Logger logger;

	private static final String DEFAULT_PATTERN = "%d [%t] %X{requestResponseKey} - %m%n";

	public CustomFeignLogger(Class<?> clazz) {
		this(clazz.getName(), null);
	}

	public CustomFeignLogger(String name, LogTraceProperties properties) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		AppenderRef[] refs = null;
		Layout<?> layout = PatternLayout.createLayout(
				(properties == null || properties.getPatter() == null) ? DEFAULT_PATTERN : properties.getPatter(), null,
				config, null, Charset.forName("UTF-8"), true, false, null, null);

		Appender fileAppender = FileAppender.createAppender(
				(properties == null || properties.getLogFile() == null) ? (File.separator + "var" + File.separator
						+ "log" + File.separator + "hzcard" + File.separator + "feignClient.log")
						: properties.getLogFile(),
				null, null, name + AppenderEnum.FILE, null, null, "true", "16384", layout, null, null, null, config);
		fileAppender.start();
		config.addAppender(fileAppender);
		Appender kafkaAppender = null;
		if (properties.getKafkaProperty() != null) {
			Property[] kafkaProperties = new Property[properties.getKafkaProperty().size()];
			int i = 0;
			for (LogTraceProperty sProperty : properties.getKafkaProperty().values()) {
				kafkaProperties[i] = Property.createProperty(sProperty.getName(), sProperty.getValue());
				i++;
			}
			kafkaAppender = HzcardKafkaAppender.createAppender(layout, null, name + AppenderEnum.KAFKA.toString(), true,
					properties.getKafkaTopic(), kafkaProperties);
			kafkaAppender.start();
			config.addAppender(kafkaAppender);
		}
		LoggerConfig loggerConfig = null;
		if (properties.getAppenderType() == null) {
			refs = new AppenderRef[] { AppenderRef.createAppenderRef(name + AppenderEnum.FILE.toString(), null, null) };
			loggerConfig = LoggerConfig.createLogger(false, org.apache.logging.log4j.Level.DEBUG, name,
					String.valueOf(properties.isSyn()), refs, null, config, null);
			loggerConfig.addAppender(fileAppender, org.apache.logging.log4j.Level.DEBUG, null);
		} else if (properties.getAppenderType().equals(AppenderEnum.ALL)) {
			refs = new AppenderRef[] { AppenderRef.createAppenderRef(name + "FILE", null, null),
					AppenderRef.createAppenderRef(name + "KAFKA", null, null) };
			loggerConfig = LoggerConfig.createLogger(false, org.apache.logging.log4j.Level.DEBUG, name,
					String.valueOf(properties.isSyn()), refs, null, config, null);
			loggerConfig.addAppender(fileAppender, org.apache.logging.log4j.Level.DEBUG, null);
			loggerConfig.addAppender(kafkaAppender, org.apache.logging.log4j.Level.DEBUG, null);
		} else {
			refs = new AppenderRef[] {
					AppenderRef.createAppenderRef(name + properties.getAppenderType().toString(), null, null) };
			loggerConfig = LoggerConfig.createLogger(false, org.apache.logging.log4j.Level.DEBUG, name,
					String.valueOf(properties.isSyn()), refs, null, config, null);
			if (properties.getAppenderType().equals(AppenderEnum.KAFKA))
				loggerConfig.addAppender(kafkaAppender, org.apache.logging.log4j.Level.DEBUG, null);
			else
				loggerConfig.addAppender(fileAppender, org.apache.logging.log4j.Level.DEBUG, null);
		}
		config.addLogger(name, loggerConfig);
		ctx.updateLoggers(config);
		this.logger = ctx.getLogger(name);
	}

	@Override
	protected void log(String configKey, String format, Object... args) {
		try {
			logger.debug(String.format(methodTag(configKey) + format, args));
		} catch (Throwable th) {
			th.printStackTrace();
			System.out.println(th.getMessage());
		}

	}

	@Override
	protected void logRequest(String configKey, Level logLevel, Request request) {
		if (logger.isDebugEnabled()) {
			String requestResponseKey = UUID.randomUUID().toString().replace("-", "");
			ThreadContext.put("requestResponseKey", requestResponseKey);
			super.logRequest(configKey, logLevel, request);
			ThreadContext.pop();
		}
	}

	@Override
	protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
			throws IOException {
		if (logger.isDebugEnabled()) {

			Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>();

			for (String headerFild : response.headers().keySet()) {
				headers.put(headerFild, response.headers().get(headerFild));
			}
			if (HystrixRequestContext.getContextForCurrentThread() != null) {

				HystrixRequestVariableDefault<Map<EventIimmutable, Integer>> requestVariable = RequestVariablHolder
						.getInstance();
				Map<EventIimmutable, Integer> ievent = null;
				try {
					ievent = requestVariable.get();
				} catch (Throwable e) {
					// logger.error("RequestInterceptor exception ",e);
				}
				if (ievent != null) {
					Set<EventIimmutable> rEvents = ievent.keySet();
					for (EventIimmutable rEvent : rEvents) {
						if (rEvent.getEventPlatform() != null)
							headers.put(IEventConstant.X_EVENT_PLATFORM,
									Arrays.asList(new String[] { rEvent.getEventPlatform() }));
						if (rEvent.getEventType() != null)
							headers.put(IEventConstant.X_EVENT_TYPE,
									Arrays.asList(new String[] { rEvent.getEventType() }));
						if (rEvent.getEventId() != null)
							headers.put(IEventConstant.X_EVENT_ID, Arrays.asList(rEvent.getEventId()));
						if (rEvent.getEventCode() != null)
							headers.put(IEventConstant.X_EVENT_CODE, Arrays.asList(rEvent.getEventCode()));
						if (rEvent.getEventSequence() != null)
							headers.put(IEventConstant.X_EVENT_SEQUENCE,
									Arrays.asList(rEvent.getEventSequence() + "-" + ievent.get(rEvent)));
						break;
					}
				}
			}
			response = Response.builder().body(response.body()).status(response.status()).headers(headers)
					.reason(response.reason()).build();
			return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
		}
		return response;
	}

}
