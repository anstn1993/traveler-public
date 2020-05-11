package me.moonsoo.travelerrestapi.accompany.childcomment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyCommentSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
public class AccompanyChildComment {

    @Id @GeneratedValue
    private Integer id;

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;

    @ManyToOne(targetEntity = AccompanyComment.class)
    @JsonSerialize(using = AccompanyCommentSerializer.class)
    private AccompanyComment accompanyComment;

    @NotBlank
    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime regDate;



}
