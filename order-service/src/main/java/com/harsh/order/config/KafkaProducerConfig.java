package com.harsh.order.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.harsh.common.event.OrderCreatedEvent;

@Configuration
public class KafkaProducerConfig {

	@Bean
	public ProducerFactory<String, OrderCreatedEvent> producerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();

//		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(props);

	}

	@Bean
	public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate(KafkaProperties properties) {

		return new KafkaTemplate<>(producerFactory(properties));

	}

}