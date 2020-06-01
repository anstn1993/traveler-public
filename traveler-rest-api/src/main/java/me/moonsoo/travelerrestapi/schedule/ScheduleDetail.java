package me.moonsoo.travelerrestapi.schedule;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
public class ScheduleDetail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(targetEntity = ScheduleLocation.class)
    @JsonSerialize(using = ScheduleLocationSerializer.class)
    private ScheduleLocation scheduleLocation;

    @Column(nullable = false)
    private String place;//세부 여행지 명

    @Column(nullable = false, columnDefinition = "text")
    private String plan;//세부 일정

    @Column(nullable = false)
    private LocalDateTime startDate;//세부 일정 시작 시간

    @Column(nullable = false)
    private LocalDateTime endDate;//세부 일정 종료 시간

}
