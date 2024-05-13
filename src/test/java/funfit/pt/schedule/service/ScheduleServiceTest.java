package funfit.pt.schedule.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.Role;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.RabbitMqService;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private RelationshipRepository relationshipRepository;
    @Autowired private EntityManager em;
    private ScheduleService scheduleService;

    @BeforeEach
    public void setup() {
        scheduleService = new ScheduleService(scheduleRepository, relationshipRepository, new UserServiceStub(null, null));
    }

    private class UserServiceStub extends UserService {

        Map<String, User> userStore = new HashMap<>();

        public UserServiceStub(RedisTemplate<String, User> redisTemplateForUser, RabbitMqService rabbitMqService) {
            super(redisTemplateForUser, rabbitMqService);
            userStore.put("member@naver.com", new User(1, "member@naver.com", "member", Role.MEMBER, "01011112222", null));
            userStore.put("trainer@naver.com", new User(2, "trainer@naver.com", "trainer", Role.TRAINER, "01011112222", "userCode"));
        }

        @Override
        public User getUser(String email) {
            System.out.println("~~~");
            return userStore.get(email);
        }
    }

    @Test
    @DisplayName("스케줄 조회 성공")
    public void readScheduleTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        scheduleRepository.save(Schedule.create(relationship, LocalDateTime.of(2024, 1, 1, 18, 00)));
        scheduleRepository.save(Schedule.create(relationship, LocalDateTime.of(2024, 1, 1, 19, 00)));

        // when
        ReadScheduleResponse responseDto = scheduleService.readSchedule("member@naver.com");

        // then
        org.assertj.core.api.Assertions.assertThat(responseDto.getReadUserRole()).isEqualTo(Role.MEMBER.getName());
        org.assertj.core.api.Assertions.assertThat(responseDto.getReservedTimeList().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("스케줄 추가 성공")
    public void addScheduleTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 18, 00);
        AddScheduleRequest requestDto = new AddScheduleRequest(dateTime);

        // when
        AddScheduleResponse responseDto = scheduleService.addSchedule(requestDto, "member@naver.com");

        // then
        assertThat(responseDto.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    @DisplayName("스케줄 추가 실패-이미 예약된 시간")
    public void addScheduleTestFailByDuplicatedDate() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        // when
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 18, 00);
        scheduleRepository.save(Schedule.create(relationship, dateTime));

        // then
        AddScheduleRequest requestDto = new AddScheduleRequest(dateTime);
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scheduleService.addSchedule(requestDto, "member@naver.com");
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_RESERVATION);
    }
}
