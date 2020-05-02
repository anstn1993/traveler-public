package me.moonsoo.travelerrestapi.accompany;

import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


//request body의 데이터를 바인딩할 모델 클래스
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccompanyDto {

    @NotBlank
    private String title;//게시물 제목
    @NotBlank
    private String article;//본문
    @NotNull
    private LocalDateTime startDate;//여행 시작 시간
    @NotNull
    private LocalDateTime endDate;//여행 종료 시간
    @NotBlank
    private String location;//여행 장소명
    @NotNull
    private Double latitude;//여행 장소 위도
    @NotNull
    private Double longitude;//여행 장소 경도

}
