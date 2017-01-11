package com.hzcard.logtrace;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.mom.kafka.DefaultKafkaProducerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaProducerFactory;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.util.Log4jThread;

public class HzcardKafkaManager extends AbstractManager {

	public static final String DEFAULT_TIMEOUT_MILLIS = "30000";

	/**
	 * package-private access for testing.
	 */
	static KafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory();

	private final Properties config = new Properties();
	private Producer<byte[], byte[]> producer;
	private final int timeoutMillis;

	private final String topic;
	private final boolean syncSend;

	public HzcardKafkaManager(final String name, final String topic,
			final boolean syncSend, final Property[] properties) {
		super(name);
		this.topic = Objects.requireNonNull(topic, "topic");
		this.syncSend = syncSend;
		config.setProperty("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		config.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		config.setProperty("batch.size", "0");
		for (final Property property : properties) {
			config.setProperty(property.getName(), property.getValue());
		}
		this.timeoutMillis = Integer.parseInt(config.getProperty("timeout.ms", DEFAULT_TIMEOUT_MILLIS));
	}

	@Override
	public void releaseSub() {
		closeProducer(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	private void closeProducer(final long timeout, final TimeUnit timeUnit) {
		if (producer != null) {
            // This thread is a workaround for this Kafka issue: https://issues.apache.org/jira/browse/KAFKA-1660
            final Thread closeThread = new Log4jThread(new Runnable() {
                @Override
                public void run() {
                    producer.close();
                }
            });
            closeThread.setName("KafkaManager-CloseThread");
            closeThread.setDaemon(true); // avoid blocking JVM shutdown
            closeThread.start();
            try {
                closeThread.join(timeoutMillis);
            } catch (final InterruptedException ignore) {
                // ignore
            }
        }
	}

	public void send(final byte[] msg) throws ExecutionException, InterruptedException, TimeoutException {
		if (producer != null) {
			Future<RecordMetadata> response = producer.send(new ProducerRecord<byte[], byte[]>(topic, msg));
			if (syncSend) {
				response.get(timeoutMillis, TimeUnit.MILLISECONDS);
			}
		}
	}

	public void startup() {
		producer = producerFactory.newKafkaProducer(config);
	}

	public String getTopic() {
		return topic;
	}

}
