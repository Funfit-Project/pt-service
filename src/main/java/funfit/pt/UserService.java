package funfit.pt;

import com.fasterxml.jackson.core.JsonProcessingException;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.RabbitMqService;
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
        // 캐시에 사용자가 있는지 확인 후, 없으면 MQ를 통해 받아온 후 저장
        UserDto user = (UserDto) redisTemplate.opsForValue().get(email);
        if (user == null) {
            rabbitMqService.requestUserByEmail(new RequestUserByEmail(email));
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email));
    }

    public ResponseValidateTrainerCode getTrainerDto(String userCode) {
        //TODO 캐시에 있는지 확인 먼저 하기
        return rabbitMqService.requestValidateTrainerCode(new RequestValidateTrainerCode(userCode));
    }
}
