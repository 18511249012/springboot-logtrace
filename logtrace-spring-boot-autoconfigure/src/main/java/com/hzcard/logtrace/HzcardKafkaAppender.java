package com.hzcard.logtrace;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.util.StringEncoder;

/**
 * Sends log events to an Apache Kafka topic.
 */
@Plugin(name = "Kafka", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class HzcardKafkaAppender extends AbstractAppender {

	@PluginFactory
	public static HzcardKafkaAppender createAppender(@PluginElement("Layout") final Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter,
			@Required(message = "No name provided for KafkaAppender") @PluginAttribute("name") final String name,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
			@Required(message = "No topic provided for KafkaAppender") @PluginAttribute("topic") final String topic,
			@PluginElement("Properties") final Property[] properties) {
		final HzcardKafkaManager kafkaManager = new HzcardKafkaManager(name, topic,false, properties);
		return new HzcardKafkaAppender(name, layout, filter, ignoreExceptions, kafkaManager);
	}

	private final HzcardKafkaManager manager;

	private HzcardKafkaAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final boolean ignoreExceptions, final HzcardKafkaManager manager) {
        super(name, filter, layout, ignoreExceptions);
        this.manager = manager;
    }

	@Override
	public void append(final LogEvent event) {
		if (event.getLoggerName().startsWith("org.apache.kafka")) {
			LOGGER.warn("Recursive logging from [{}] for appender [{}].", event.getLoggerName(), getName());
		} else {
			try {
				final Layout<? extends Serializable> layout = getLayout();
				byte[] data;
				if (layout != null) {
					if (layout instanceof SerializedLayout) {
						final byte[] header = layout.getHeader();
						final byte[] body = layout.toByteArray(event);
						data = new byte[header.length + body.length];
						System.arraycopy(header, 0, data, 0, header.length);
						System.arraycopy(body, 0, data, header.length, body.length);
					} else {
						data = layout.toByteArray(event);
					}
				} else {
					data = StringEncoder.toBytes(event.getMessage().getFormattedMessage(), StandardCharsets.UTF_8);
				}
				manager.send(data);
			} catch (final Exception e) {
				LOGGER.error("Unable to write to Kafka [{}] for appender [{}].", manager.getName(), getName(), e);
				throw new AppenderLoggingException("Unable to write to Kafka in appender: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void start() {
		super.start();
		manager.startup();
	}

	@Override
	public void stop() {
		super.stop();
		manager.release();
	}

	@Override
	public String toString() {
		return "KafkaAppender{" + "name=" + getName() + ", state=" + getState() + ", topic=" + manager.getTopic() + '}';
	}

}
