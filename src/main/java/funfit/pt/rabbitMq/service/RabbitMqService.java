package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.*;
import funfit.pt.api.dto.User;
import funfit.pt.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RedisTemplate<String, User> redisTemplate;
    private final RelationshipService relationshipService;

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
