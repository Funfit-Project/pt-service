package funfit.pt.api;

import funfit.pt.api.AuthServiceClient;
import funfit.pt.api.dto.User;
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
    private final AuthServiceClient authServiceClient;

    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public User getUser(String email) {
        User user = redisTemplateForUser.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return authServiceClient.getUser(email);
    }

    private User fallback(String email, Throwable e) {
        log.error("레디스 장애로 인한 fallback 메소드 호출, {}", e.getMessage());
        return authServiceClient.getUser(email);
    }
}
