package funfit.pt.schedule.repository;

import funfit.pt.schedule.entity.Schedule;
import funfit.pt.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s " +
            "where s.relationship.trainer = :trainer")
    List<Schedule> findByTrainer(@Param("trainer") User trainer);
}
