package funfit.pt.schedule.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired private ScheduleService scheduleService;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private RelationshipRepository relationshipRepository;
    @Autowired private EntityManager em;

    @BeforeEach
    public void initRelationship() {
        em.createNativeQuery("alter table relationship alter column relationship_id restart with 1;")
                .executeUpdate();

        Relationship relationship = Relationship.create(2, 1, "펀핏짐", 10);
        relationshipRepository.save(relationship);
    }

    @Test
    @DisplayName("스케줄 추가 성공")
    public void addScheduleTestSuccess() {
        // given
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 18, 00);
        String memo = "하체운동하는 날";
        AddScheduleRequest requestDto = new AddScheduleRequest(date, memo);

        // when
        AddScheduleResponse responseDto = scheduleService.addSchedule(requestDto, 1);

        // then
        assertThat(responseDto.getDate()).isEqualTo(date);
        assertThat(responseDto.getMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("스케줄 추가 실패-잘못된 relationship id")
    public void addScheduleTestFailByInvalidRelationshipId() {
        // given
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 18, 00);
        String memo = "하체운동하는 날";
        AddScheduleRequest requestDto = new AddScheduleRequest(date, memo);

        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scheduleService.addSchedule(requestDto, 2);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_RELATIONSHIP_ID);
    }

    @Test
    @DisplayName("스케줄 추가 실패-이미 예약된 시간")
    public void addScheduleTestFailByDuplicatedDate() {
        // given
        Relationship otherRelationship = Relationship.create(3, 1, "펀핏짐", 10);
        relationshipRepository.save(otherRelationship);
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 18, 00);
        String memo = "하체운동하는 날";
        Schedule otherSchedule = Schedule.create(otherRelationship, date, memo);
        scheduleRepository.save(otherSchedule);

        // when
        AddScheduleRequest requestDto = new AddScheduleRequest(date, memo);

        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scheduleService.addSchedule(requestDto, 1);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_RESERVATION);
    }
}
