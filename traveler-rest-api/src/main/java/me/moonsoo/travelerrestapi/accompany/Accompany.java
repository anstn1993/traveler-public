package me.moonsoo.travelerrestapi.accompany;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.travelerrestapi.account.Account;
import me.moonsoo.travelerrestapi.account.AccountSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;



//동행 구하기 게시판의 게시물을 저장하는 엔티티
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
public class Accompany {

    @Id @GeneratedValue
    private Integer id;

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;//게시물 업로더

    @Column(nullable = false)
    private String title;//게시물 제목

    @Column(nullable = false)
    private String article;//본문

    @Column(nullable = false)
    private LocalDateTime startDate;//여행 시작 시간

    @Column(nullable = false)
    private LocalDateTime endDate;//여행 종료 시간

    @Column(nullable = false)
    private String location;//여행 장소명

    @Column(nullable = false, columnDefinition = "decimal(13, 10)")
    private Double latitude;//여행 장소 위도

    @Column(nullable = false, columnDefinition = "decimal(13, 10)")
    private Double longitude;//여행 장소 경도

    @Column(nullable = false)
    private LocalDateTime regDate;//게시물 등록 시간
}
