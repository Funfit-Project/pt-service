package funfit.pt.api;

import funfit.pt.api.dto.User;
import funfit.pt.exception.customException.ExternalServiceFailureException;
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
public class UserDataProvider {

    private final RedisTemplate<String, User> redisTemplate;
    private final AuthServiceClient authServiceClient;

    /*
        - 레디스에서 조회 -> null 또는 레디스 장애 시 auth 서비스에서 조회
        - auth 서비스에서 조회 -> 장애 시 "알수없음"으로 사용자명 반환
    */
    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public User getUser(String email) {
        User user = redisTemplate.opsForValue().get(email);
        if (user == null) {
            try {
                user = authServiceClient.getUserByEmail(email);
                redisTemplate.opsForValue().set(email, user);
            } catch (ExternalServiceFailureException exception) {
                log.info("catch ExternalServiceFailureException");
                return null;
            }
        }
        return user;
    }

    public User fallback(String email, Exception exception) {
        log.info("레디스 장애로 인한 fallback 메소드 호출: {}", exception.getMessage());
        try {
            return authServiceClient.getUserByEmail(email);
        } catch (ExternalServiceFailureException externalServiceFailureException) {
            return null;
        }
    }
}
