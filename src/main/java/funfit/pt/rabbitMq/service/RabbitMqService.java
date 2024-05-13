package funfit.pt.rabbitMq.service;

import funfit.pt.rabbitMq.dto.*;
import funfit.pt.relationship.service.RelationshipService;
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
    private final RelationshipService relationshipService;

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

    @RabbitListener(queues = "create_new_member")
    public void createRelationship(CreateNewMemberSubDto dto) {
        log.info("RabbitMQ | on message in create_new_member queue, message = {}", dto.toString());
        relationshipService.createRelationship(dto.getMemberId(), dto.getTrainerId(), dto.getCenterName(), dto.getRegistrationCount());
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
