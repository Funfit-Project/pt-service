package funfit.pt.rabbitMq.service;

import funfit.pt.api.AuthServiceClient;
import funfit.pt.rabbitMq.dto.*;
import funfit.pt.api.dto.User;
import funfit.pt.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RedisTemplate<String, User> redisTemplate;
    private final RelationshipService relationshipService;
    private final AuthServiceClient authServiceClient;

    /**
     * 새로운 PT 회원 생성 시 -> Relationship 생성 후 DB 저장
     */
    @RabbitListener(queues = "create_new_member")
    public void createRelationship(CreateNewMemberSubDto dto) {
        log.info("RabbitMQ | on message, queue name = create_new_member, message = {}", dto.toString());
        relationshipService.createRelationship(dto.getMemberEmail(), dto.getTrainerEmail(), dto.getCenterName(), dto.getRegistrationCount());
    }

    /**
     * 회원 정보 변경 시 -> id를 통해 회원 정보 요청
     */
    @RabbitListener(queues = "edited_user_email_for_pt")
    public void onMessageInEditedUserEmail(String email) {
        log.info("RabbitMQ | on message, queue name = edited_user_email_for_pt queue, message = {}", email);
        User user = authServiceClient.getUserByEmail(email);
        redisTemplate.opsForValue().set(user.getEmail(), user);
    }
}
