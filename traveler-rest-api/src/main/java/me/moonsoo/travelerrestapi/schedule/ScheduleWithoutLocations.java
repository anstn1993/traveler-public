package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "schedule")
public class ScheduleWithoutLocations {

    @Id
    @GeneratedValue
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
}
