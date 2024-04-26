package funfit.pt.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AddScheduleRequest {

    private LocalDateTime date;
    private String memo;
}
