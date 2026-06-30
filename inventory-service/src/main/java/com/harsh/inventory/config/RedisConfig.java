package com.harsh.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

	@Bean
	public JedisConnectionFactory jedisConnectionFactory(RedisProperties props) {

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(props.getHost(), props.getPort());

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(props.getPool().getMaxTotal());
		poolConfig.setMaxIdle(props.getPool().getMaxIdle());
		poolConfig.setMinIdle(props.getPool().getMinIdle());
		poolConfig.setMaxWait(props.getPool().getMaxWait());

		poolConfig.setTestOnBorrow(props.getPool().isTestOnBorrow());
		poolConfig.setTestOnReturn(props.getPool().isTestOnReturn());
		poolConfig.setTestWhileIdle(props.getPool().isTestWhileIdle());

		JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
				.build();

		return new JedisConnectionFactory(redisConfig, clientConfig);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory connectionFactory) {

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer serializer = new StringRedisSerializer();
		template.setKeySerializer(serializer);
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(serializer);
		template.setHashValueSerializer(serializer);

		return template;
	}
}
