package funfit.pt.schedule.repository;

import funfit.pt.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s " +
            "where s.relationship.trainerId = :trainerId")
    List<Schedule> findByTrainerUserId(@Param("trainerId") long trainerId);
}
