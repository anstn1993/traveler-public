package me.moonsoo.travelerrestapi.accompany.comment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanySerializer;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "accompany_comment")
public class AccompanyComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;//댓글 id

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;//댓글 작성자

    @ManyToOne(targetEntity = Accompany.class)
    @JsonSerialize(using = AccompanySerializer.class)
    private Accompany accompany;//해당 댓글의 게시물

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime regDate;
}
