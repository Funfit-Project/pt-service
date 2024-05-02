package funfit.pt.rabbitMq;

import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.ResponseUser;
import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void requestUserByEmail(RequestUserByEmail dto) {
        log.info("RabbitMQ| request user by email, user email = {}", dto.getEmail());
        rabbitTemplate.convertAndSend("user_request_by_email", dto);
        log.info("RabbitMQ| success send messages");
    }

    @RabbitListener(queues = "user")
    public void onMessageInUser(final ResponseUser dto) {
        log.info("RabbitMQ| on message in user, user = {}", dto.toString());
        redisTemplate.opsForValue().set(dto.getEmail(), dto);
        log.info("사용자 정보 캐시에 저장 완료");
    }

    public void requestValidateTrainerCode(RequestValidateTrainerCode dto) {
        log.info("RabbitMQ| request validate trainer code, trainer code = {}", dto.getTrainerCode());
        rabbitTemplate.convertAndSend("request_validate_trainer_code", dto);
        log.info("RabbitMQ| success send messages");
    }

    @RabbitListener(queues = "response_validate_trainer_code")
    public void onMessageInUser(final ResponseValidateTrainerCode dto) {
        log.info("RabbitMQ| on message in response validate trainer code");
        redisTemplate.opsForValue().set(dto.getTrainerCode(), dto);
    }
}
