package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import funfit.pt.rabbitMq.entity.User;
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

    public User getUser(String email) {
        System.out.println("!!!!");
        User user = redisTemplateForUser.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "pt"));
    }
}
