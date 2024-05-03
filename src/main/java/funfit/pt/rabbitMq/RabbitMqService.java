package funfit.pt.rabbitMq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    public UserDto requestUserByEmail(RequestUserByEmail dto) throws JsonProcessingException {
        String messageBody = mapper.writeValueAsString(dto);

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setCorrelationId(dto.getEmail());

        Message message = new Message(messageBody.getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("request_user_by_email", message);

        UserDto userDto = mapper.readValue(new String(response.getBody()), UserDto.class);
        log.info("RabbitMQ | 사용자 정보 획득, userDto = {}", userDto.toString());
        redisTemplate.opsForValue().set(userDto.getEmail(), userDto);
        log.info("Redis | 사용자 정보 캐시 저장 완료");
        return userDto;
    }

    public ResponseValidateTrainerCode requestValidateTrainerCode(RequestValidateTrainerCode dto) throws JsonProcessingException {
        String messageBody = mapper.writeValueAsString(dto);
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setCorrelationId(dto.getTrainerCode());

        Message message = new Message(messageBody.getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("request_validate_trainer_code", message);

        ResponseValidateTrainerCode responseDto = mapper.readValue(new String(response.getBody()), ResponseValidateTrainerCode.class);
        redisTemplate.opsForValue().set(responseDto.getTrainerCode(), responseDto);

        System.out.println(responseDto.getResult());
        if (!responseDto.getResult()) {
            throw new BusinessException(ErrorCode.INVALID_USER_CODE);
        }

        return responseDto;
    }
}
