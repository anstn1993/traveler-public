package me.moonsoo.travelerrestapi.post.like;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostSerializer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity(name = "post_like")
public class Like {

    @Id
    @GeneratedValue
    private Integer id;//pk

    @ManyToOne(targetEntity = Post.class)
    @JsonSerialize(using = PostSerializer.class)
    private Post post;

    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;

}
