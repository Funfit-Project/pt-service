package funfit.pt.rabbitMq.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, UserDto> redisTemplate;

    public UserDto requestUserByEmail(RequestUserByEmail dto) {
        log.info("RabbitMQ | publish message in request_user_by_email queue");
        Object message = rabbitTemplate.convertSendAndReceive("request_user_by_email", dto);
        log.info("RabbitMQ | response message = {}", message.toString());

        UserDto userDto = convertMessageToUserDto(message);
        redisTemplate.opsForValue().set(userDto.getEmail(), userDto);
        log.info("Redis | 사용자 정보 저장 완료");
        return userDto;
    }

    private UserDto convertMessageToUserDto(Object response) {
        LinkedHashMap map = (LinkedHashMap) response;
        UserDto dto = new UserDto();

        dto.setUserId((Integer)map.get("userId"));
        dto.setEmail((String)map.get("email"));
        dto.setUserName((String)map.get("userName"));
        dto.setRoleName((String)map.get("roleName"));
        dto.setPhoneNumber((String)map.get("phoneNumber"));
        dto.setUserCode((String)map.get("userCode"));
        return dto;
    }

    public ResponseValidateTrainerCode requestValidateTrainerCode(RequestValidateTrainerCode dto) {
        Object message = rabbitTemplate.convertSendAndReceive("request_validate_trainer_code", dto);
        log.info("response message = {}", message.toString());

        ResponseValidateTrainerCode validateTrainerCodeDto = convertMessageToValidateTrainerCodeDto(message);
        if (!validateTrainerCodeDto.getResult()) {
            throw new BusinessException(ErrorCode.INVALID_USER_CODE);
        }
        return validateTrainerCodeDto;
    }

    private ResponseValidateTrainerCode convertMessageToValidateTrainerCodeDto(Object response) {
        LinkedHashMap map = (LinkedHashMap) response;
        ResponseValidateTrainerCode dto = new ResponseValidateTrainerCode();

        dto.setResult((boolean)map.get("result"));
        dto.setTrainerUserId((Integer)map.get("trainerUserId"));
        dto.setUserName((String)map.get("userName"));
        dto.setTrainerCode((String)map.get("trainerCode"));
        return dto;
    }

    @RabbitListener(queues = "edited_user_id_for_pt")
    public void onMessageInEditedUserIdForPT(long userId) {
        log.info("RabbitMQ | on message in edited_user_id_for_pt queue, message = {}", userId);
        String url = "http://localhost:8081/userInfo/pt/" + userId;
        RestTemplate restTemplate = new RestTemplate();
        UserDto userDto = restTemplate.getForObject(url, UserDto.class);
        redisTemplate.opsForValue().set(userDto.getEmail(), userDto);
        log.info("Redis | 사용자 정보 수정사항 반영 완료");
    }
}
