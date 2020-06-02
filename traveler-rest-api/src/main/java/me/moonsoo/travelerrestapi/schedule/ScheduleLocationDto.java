package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleLocationDto {

    @NotBlank
    private String location;//위치 명

    @NotNull
    private Double latitude;//장소 위도

    @NotNull
    private Double longitude;//장소 경도

    @NotEmpty
    @JsonProperty("scheduleDetails")//json key 설정
    private LinkedHashSet<ScheduleDetailDto> scheduleDetailDtos = new LinkedHashSet<>();

}
