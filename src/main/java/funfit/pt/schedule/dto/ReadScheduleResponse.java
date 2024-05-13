package funfit.pt.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReadScheduleResponse {

    private String readUserRole;
    private List<ScheduleDto> reservedTimeList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {

        private LocalDateTime dateTime;
        private String memberName;
    }
}
