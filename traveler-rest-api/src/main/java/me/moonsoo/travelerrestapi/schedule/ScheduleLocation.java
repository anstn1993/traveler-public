package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "schedule", "location", "latitude", "longitude"})
@Builder
@Entity
public class ScheduleLocation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(targetEntity = Schedule.class)
    @JsonSerialize(using = ScheduleSerializer.class)
    private Schedule schedule;

    @Column(nullable = false)
    private String location;//위치 명

    @Column(nullable = false, columnDefinition = "decimal(13, 10)")
    private Double latitude;//장소 위도

    @Column(nullable = false, columnDefinition = "decimal(13, 10)")
    private Double longitude;//장소 경도

    @OneToMany(mappedBy = "scheduleLocation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ScheduleDetail> scheduleDetails = new LinkedHashSet<>();//세부 일정

    private void addScheduleDetails(ScheduleDetail scheduleDetail) {
        scheduleDetail.setScheduleLocation(this);
        this.scheduleDetails.add(scheduleDetail);
    }
}
