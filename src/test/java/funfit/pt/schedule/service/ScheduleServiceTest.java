package funfit.pt.schedule.service;

import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.Role;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
@Rollback(value = false)
class ScheduleServiceTest {

    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private RelationshipRepository relationshipRepository;
    @Autowired private ScheduleService scheduleService;

    @TestConfiguration
    static class TestConfig {

        @Autowired private ScheduleRepository scheduleRepository;
        @Autowired private RelationshipRepository relationshipRepository;

        @Bean
        public ScheduleService scheduleService() {
            return new ScheduleService(scheduleRepository, relationshipRepository, userServiceStub());
        }

        @Bean
        public UserServiceStub userServiceStub() {
            return new UserServiceStub();
        }
    }

    static class UserServiceStub extends UserService {

        Map<String, User> userStore = new HashMap<>();

        public UserServiceStub() {
            super(null, null);
            userStore.put("member@naver.com", new User(1, "member@naver.com", "member", Role.MEMBER, "01011112222", null));
            userStore.put("trainer@naver.com", new User(2, "trainer@naver.com", "trainer", Role.TRAINER, "01011112222", "userCode"));
        }

        @Override
        public User getUser(String email) {
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
        assertThat(responseDto.getReadUserRole()).isEqualTo(Role.MEMBER.getName());
        assertThat(responseDto.getReservedTimeList().size()).isEqualTo(2);
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
    @DisplayName("스케줄 추가 실패-이미 예약된 시간(동시성 제어)")
    public void addScheduleTestFailByDuplicatedDate() throws InterruptedException {
        // given
        int numberOfThreads = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 18, 00);
        String trainerEmail = "trainer@naver.com";

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            String memberEmail = "member" + i + "@naver.com";

            executorService.execute(() -> {
                // relationship 생성 및 저장
                Relationship relationship = Relationship.create(memberEmail, trainerEmail, "펀핏짐", 10);
                relationshipRepository.save(relationship);

                // scheduleService.addSchedule() 호출
                AddScheduleRequest requestDto = new AddScheduleRequest(dateTime);
                try {
                    scheduleService.addSchedule(requestDto, memberEmail);
                } catch (BusinessException e) {
                    log.info("catch BusinessException, message = {}", e.getErrorCode().getMessage());
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        // then
        int size = scheduleRepository.findByTrainerEmail(trainerEmail)
                .stream()
                .filter(schedule -> schedule.getDateTime().equals(dateTime))
                .toList()
                .size();
        assertThat(size).isEqualTo(1);
    }
}
