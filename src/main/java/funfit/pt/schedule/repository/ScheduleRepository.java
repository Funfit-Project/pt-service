package funfit.pt.schedule.repository;

import funfit.pt.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByTrainerEmailAndDateTime(String trainerEmail, LocalDateTime dateTime);

    @Query("select s from Schedule s " +
            "where s.dateTime >= :startOfWeek and s.dateTime <= :endOfWeek")
    List<Schedule> findByWeek(@Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);

    List<Schedule> findByTrainerEmail(String trainerEmail);
}
