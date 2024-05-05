package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplate redisTemplate;
    private final RabbitMqService rabbitMqService;

    public UserDto getUserDto(String email) {
        UserDto user = (UserDto) redisTemplate.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email));
    }

    public ResponseValidateTrainerCode getTrainerDto(String userCode) {
        ResponseValidateTrainerCode trainerCode = (ResponseValidateTrainerCode) redisTemplate.opsForValue().get(userCode);
        if (trainerCode != null) {
            return trainerCode;
        }
        return rabbitMqService.requestValidateTrainerCode(new RequestValidateTrainerCode(userCode));
    }
}
