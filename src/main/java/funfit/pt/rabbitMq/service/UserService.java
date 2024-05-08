package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
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

    private final RedisTemplate<String, UserDto> redisTemplateForUserDto;
    private final RabbitMqService rabbitMqService;

    public UserDto getUserDto(String email) {
        UserDto userDto = redisTemplateForUserDto.opsForValue().get(email);
        if (userDto != null) {
            return userDto;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "pt"));
    }

    public ResponseValidateTrainerCode getTrainerDto(String userCode) {
        return rabbitMqService.requestValidateTrainerCode(new RequestValidateTrainerCode(userCode));
    }
}
