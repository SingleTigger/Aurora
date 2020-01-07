package com.chenws.iot.common.config;

import com.chenws.iot.common.utils.RedisUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by chenws on 2019/10/9.
 */
@Configuration
public class RedisTemplateConfig {

	@Bean("jsonRedisTemplate")
	public RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String,Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		//使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper mapper = new ObjectMapper();
		// 是否需要排序
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		// 忽略空bean转json的错误
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		// 取消默认转换timestamps形式
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		// 序列化的时候，过滤null属性
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		// 忽略在json字符串中存在，但在java对象中不存在对应属性的情况，防止错误
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// 忽略空bean转json的错误
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		//支持多态类型数据绑定，序列化时加上类信息
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		serializer.setObjectMapper(mapper);
		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(serializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean("jdkRedisTemplate")
	public RedisTemplate jdkRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String,Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public RedisUtil redisUtil(RedisTemplate<String, Object> jsonRedisTemplate){
		return new RedisUtil(jsonRedisTemplate);
	}
}
