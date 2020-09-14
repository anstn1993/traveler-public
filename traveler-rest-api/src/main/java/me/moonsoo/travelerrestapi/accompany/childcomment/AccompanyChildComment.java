package me.moonsoo.travelerrestapi.accompany.childcomment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanySerializer;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyCommentSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.ZonedDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity(name = "accompany_child_comment")
public class AccompanyChildComment {

    @Id @GeneratedValue
    private Integer id;

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;

    @ManyToOne
    @JsonSerialize(using = AccompanySerializer.class)
    private Accompany accompany;

    @ManyToOne(targetEntity = AccompanyComment.class)
    @JsonSerialize(using = AccompanyCommentSerializer.class)
    private AccompanyComment accompanyComment;

    @NotBlank
    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private ZonedDateTime regDate;



}
