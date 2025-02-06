package funfit.pt.kafka;

import funfit.pt.api.AuthServiceClient;
import funfit.pt.api.dto.User;
import funfit.pt.kafka.dto.PtMemberJoinedDto;
import funfit.pt.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final RelationshipService relationshipService;
    private final AuthServiceClient authServiceClient;
    private final CacheManager cacheManager;
    private final KafkaProducerService kafkaProducerService;

    /**
     * 회원 정보 변경 시 -> email을 통해 회원 정보 요청
     */
    @KafkaListener(
            topics = "user-info-updated",
            groupId = "pt-service-group",
            containerFactory = "kafkaListenerContainerFactoryForString"
    )
    public void consumeUserInfoUpdated(String email, Acknowledgment acknowledgment) {
        log.info("kafka consume user-info-updated, message = {}", email);
        User user = authServiceClient.getUserByEmail(email);
        cacheManager.getCache("user").put(email, user);
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "user-info-updated.DLT",
            groupId = "pt-service-group",
            containerFactory = "kafkaListenerContainerFactoryForString"
    )
    public void consumeUserInfoUpdatedDLQ(String email, Acknowledgment acknowledgment) {
        log.info("kafka consume user-info-updated.DLT, message = {}", email);
        kafkaProducerService.publishUserInfoUpdated(email);
        acknowledgment.acknowledge();
    }

    /**
     * 새로운 PT 회원 생성 시 -> Relationship 생성 후 DB 저장
     */
    @KafkaListener(
            topics = "pt-member-joined",
            groupId = "pt-service-group",
            containerFactory = "kafkaListenerContainerFactoryForCompensatePointsDto"
    )
    public void consumePtMemberJoined(PtMemberJoinedDto dto, Acknowledgment acknowledgment) {
        log.info("consume message, message = {}", dto);
        relationshipService.createRelationship(dto.getMemberEmail(), dto.getTrainerEmail(), dto.getCenterName(), dto.getRegistrationCount());
        acknowledgment.acknowledge();
    }
}
