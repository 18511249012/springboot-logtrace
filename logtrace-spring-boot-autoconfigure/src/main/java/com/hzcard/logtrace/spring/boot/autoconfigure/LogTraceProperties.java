package com.hzcard.logtrace.spring.boot.autoconfigure;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = LogTraceProperties.LOGTRACE_PREFIX)
public class LogTraceProperties {

	protected static final String LOGTRACE_PREFIX="logtrace";
	
	private AppenderEnum appenderType;
	
	private Map<String, LogTraceProperty> kafkaProperty;
	
	private String logFile;
	
	private String kafkaTopic;
	
	private String patter;
	
	/**
	 * 是否同步
	 */
	private boolean isSyn;

	

	public boolean isSyn() {
		return isSyn;
	}

	public void setSyn(boolean isSyn) {
		this.isSyn = isSyn;
	}

	public String getPatter() {
		return patter;
	}

	public void setPatter(String patter) {
		this.patter = patter;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public AppenderEnum getAppenderType() {
		return appenderType;
	}

	public void setAppenderType(AppenderEnum appenderType) {
		this.appenderType = appenderType;
	}

	

	public Map<String, LogTraceProperty> getKafkaProperty() {
		return kafkaProperty;
	}

	public void setKafkaProperty(Map<String, LogTraceProperty> kafkaProperty) {
		this.kafkaProperty = kafkaProperty;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public static String getLogtracePrefix() {
		return LOGTRACE_PREFIX;
	}
	
	
	
}
