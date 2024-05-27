package funfit.pt.rabbitMq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funfit.pt.exception.customException.RabbitMqException;
import funfit.pt.rabbitMq.dto.*;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, User> redisTemplate;
    private final RelationshipService relationshipService;
    private final ObjectMapper objectMapper;

    public User requestUserByEmail(RequestUserByEmail dto) {
        MqResult response = objectMapper.convertValue(rabbitTemplate.convertSendAndReceive("request_user_by_email", dto), MqResult.class);

        log.info("response mqResult = {}", response.toString());

        if (response.isSuccess()) {
            User user = objectMapper.convertValue(response.getResult(), User.class);
            redisTemplate.opsForValue().set(user.getEmail(), user);
            log.info("Redis | 사용자 정보 캐시 저장 완료");
            return user;
        } else {
            ErrorResponse errorResponse = (ErrorResponse) response.getResult();
            throw new RabbitMqException(errorResponse.getMessage());
        }
    }

    @RabbitListener(queues = "create_new_member")
    public void createRelationship(CreateNewMemberSubDto dto) {
        log.info("RabbitMQ | on message in create_new_member queue, message = {}", dto.toString());
        relationshipService.createRelationship(dto.getMemberEmail(), dto.getTrainerEmail(), dto.getCenterName(), dto.getRegistrationCount());
    }

    @RabbitListener(queues = "edited_user_id_for_pt")
    public void onMessageInEditedUserIdForPT(long userId) {
        log.info("RabbitMQ | on message in edited_user_id_for_pt queue, message = {}", userId);
        String url = "http://localhost:8081/userInfo/pt/" + userId;
        RestTemplate restTemplate = new RestTemplate();
        User userDto = restTemplate.getForObject(url, User.class);
        redisTemplate.opsForValue().set(userDto.getEmail(), userDto);
        log.info("Redis | 사용자 정보 수정사항 반영 완료");
    }
}
