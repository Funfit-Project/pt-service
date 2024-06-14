package funfit.pt.config;

import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.api.dto.User;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("pt_redis_sentinel_1", 26379)
                .sentinel("pt_redis_sentinel_2", 26379)
                .sentinel("pt_redis_sentinel_3", 26379);
        sentinelConfiguration.setPassword("1234");

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(sentinelConfiguration);

        return connectionFactory;
    }

    @Bean
    public RedisTemplate<String, User> redisTemplateForUser() {
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(User.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ResponseValidateTrainerCode> redisTemplateForTrainerCode() {
        RedisTemplate<String, ResponseValidateTrainerCode> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(ResponseValidateTrainerCode.class));
        return redisTemplate;
    }
}
