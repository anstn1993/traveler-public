package me.moonsoo.travelerrestapi.post;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.schedule.Schedule;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;


@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;//pk

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;//게시물 작성자

    private String article;//본문의 제목

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
    private Set<PostTag> postTagList = new LinkedHashSet<>();//게시물 태그 set

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
    private Set<PostImage> postImageList = new LinkedHashSet<>();//게시물 이미지 set

    private String location;//장소

    @Column(columnDefinition = "decimal(13, 10)")
    private Double latitude;//장소 위도

    @Column(columnDefinition = "decimal(13, 10)")
    private Double longitude;//장소 경도

    @Column(nullable = false)
    private LocalDateTime regDate;//게시물 등록 시간

    @Column(columnDefinition = "integer default 0")
    private Integer viewCount;//조회수
}
