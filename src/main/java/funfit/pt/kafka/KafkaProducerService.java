package funfit.pt.kafka;

import funfit.pt.kafka.dto.CompensatePointsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, CompensatePointsDto> kafkaTemplateForCompensatePointsDto;

    public void publishCompensatePoints(CompensatePointsDto dto) {
        log.info("kafka publish compensate-points, message = {}", dto.toString());
        kafkaTemplateForCompensatePointsDto.send("compensate-points", dto);
    }

    public void publishUserInfoUpdated(String email) {
        log.info("kafka publish user-info-updated, message = {}", email);
        kafkaTemplate.send("user-info-updated", email);
    }
}
