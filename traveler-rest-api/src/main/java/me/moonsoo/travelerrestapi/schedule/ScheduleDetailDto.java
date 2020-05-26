package me.moonsoo.travelerrestapi.schedule;

import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDetailDto {

    @NotBlank
    private String place;//세부 여행지 명

    @NotBlank
    private String plan;//세부 일정

    @NotNull
    private LocalDateTime startDate;//세부 일정 시작 시간

    @NotNull
    private LocalDateTime endDate;//세부 일정 종료 시간

}
