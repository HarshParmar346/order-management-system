package com.harsh.notification.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.harsh.common.event.InventoryFailedEvent;
import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.common.event.PaymentSuccessEvent;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, PaymentSuccessEvent> paymentSuccessConsumerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(PaymentSuccessEvent.class, false));
	}

	@Bean
	public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(PaymentFailedEvent.class, false));
	}

	@Bean
	public ConsumerFactory<String, InventoryFailedEvent> inventoryFailedConsumerFactory(
			KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(InventoryFailedEvent.class, false));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> paymentSuccessContainerFactory(
			ConsumerFactory<String, PaymentSuccessEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);

		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedContainerFactory(
			ConsumerFactory<String, PaymentFailedEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);

		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> inventoryFailedContainerFactory(
			ConsumerFactory<String, InventoryFailedEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);

		return factory;
	}
}