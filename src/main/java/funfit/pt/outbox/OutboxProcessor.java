package funfit.pt.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funfit.pt.kafka.KafkaProducerService;
import funfit.pt.kafka.dto.CompensatePointsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000)
    public void publishOutboxMessage() throws JsonProcessingException {
        List<Outbox> outboxes = outboxRepository.findAll();
        for (Outbox outbox : outboxes) {
            if (outbox.getTopic().equals("compensate-points")) {
                kafkaProducerService.publishCompensatePoints(objectMapper.readValue(outbox.getPayload(), CompensatePointsDto.class));
            }
            outboxRepository.delete(outbox);
        }
    }
}

