package funfit.pt.schedule.service;

import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.Role;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddAndDeleteScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RelationshipRepository relationshipRepository;
    @Autowired
    private ScheduleService scheduleService;

    @TestConfiguration
    static class TestConfig {

        @Autowired
        private ScheduleRepository scheduleRepository;
        @Autowired
        private RelationshipRepository relationshipRepository;

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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        for (int i = 0; i < 7; i++) {
            scheduleRepository.save(Schedule.create(relationship, dayOfWeek));
            dayOfWeek = dayOfWeek.plusDays(1);
        }

        // when
        ReadScheduleResponse responseDto = scheduleService.readScheduleForMember("member@naver.com");

        // then
        assertThat(responseDto.getTrainersReservationCount()).isEqualTo(7);
        responseDto.getReservedTimeList().stream()
                .forEach(reservationTime -> {
                    assertThat(reservationTime.isReadUsersReservation()).isTrue();
                    assertThat(reservationTime.getMemberName()).isEqualTo("member");
                });
    }

    @Test
    @DisplayName("스케줄 추가 성공")
    public void addScheduleTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 18, 00);
        AddAndDeleteScheduleRequest requestDto = new AddAndDeleteScheduleRequest(dateTime);

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
                AddAndDeleteScheduleRequest requestDto = new AddAndDeleteScheduleRequest(dateTime);
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

    @Test
    @DisplayName("스케줄 취소 성공")
    public void deleteScheduleTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 18, 00);
        AddAndDeleteScheduleRequest requestDto = new AddAndDeleteScheduleRequest(dateTime);
        scheduleService.addSchedule(requestDto, "member@naver.com");

        // then
        Assertions.assertThat(scheduleRepository.findByRelationshipAndDateTime(relationship, dateTime)).isPresent();
        scheduleService.deleteSchedule(requestDto, "member@naver.com");
        Assertions.assertThat(scheduleRepository.findByRelationshipAndDateTime(relationship, dateTime)).isEmpty();
    }
}
