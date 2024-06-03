package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.MicroServiceName;
import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import funfit.pt.rabbitMq.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplate<String, User> redisTemplateForUser;
    private final RabbitMqService rabbitMqService;

    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public User getUser(String email) {
        User user = redisTemplateForUser.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, MicroServiceName.PT));
    }

    private User fallback(String email, Throwable e) {
        log.error("레디스 장애로 인한 fallback 메소드 호출, {}", e.getMessage());
        return rabbitMqService.requestUserByEmailWithoutRedis(new RequestUserByEmail(email, MicroServiceName.PT));
    }
}
