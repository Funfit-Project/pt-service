package funfit.pt.schedule.repository;

import funfit.pt.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s " +
            "where s.relationship.trainerEmail = :trainerEmail")
    List<Schedule> findByTrainerEmail(@Param("trainerEmail") String trainerEmail);

    Optional<Schedule> findByTrainerEmailAndDateTime(String trainerEmail, LocalDateTime dateTime);
}
