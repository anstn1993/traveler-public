package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = {"id", "account", "title", "scope", "regDate", "viewCount"})
@Builder
@Entity
public class Schedule {

    @Id @GeneratedValue
    private Integer id;

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;//작성자

    @Column(nullable = false)
    private String title;//일정 게시물 제목

    @Enumerated(EnumType.STRING)
    private Scope scope;//공개 범위

    @Column(nullable = false)
    private LocalDateTime regDate;//게시물 등록 시간

    @Column(columnDefinition = "integer default 0")
    private Integer viewCount;//조회수

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ScheduleLocation> scheduleLocations = new LinkedHashSet<>();

    public void addScheduleLocation(ScheduleLocation scheduleLocation) {
        scheduleLocation.setSchedule(this);
        this.scheduleLocations.add(scheduleLocation);
    }
}
