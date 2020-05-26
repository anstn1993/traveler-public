package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

//request body의 데이터를 바인딩할 모델 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

    @NotBlank
    private String title;//일정 게시물 제목

    @NotNull
    private Scope scope;//공개 범위

    @NotEmpty
    @JsonProperty("scheduleLocations")//json key 설정
    private List<ScheduleLocationDto> scheduleLocationDtos;
}
