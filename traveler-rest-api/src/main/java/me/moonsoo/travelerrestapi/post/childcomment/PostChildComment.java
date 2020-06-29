package me.moonsoo.travelerrestapi.post.childcomment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostSerializer;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import me.moonsoo.travelerrestapi.post.comment.PostCommentSerializer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity(name = "post_child_comment")
public class PostChildComment {

    @Id @GeneratedValue
    private Integer id;

    @ManyToOne(targetEntity = Account.class)
    @JsonSerialize(using = AccountSerializer.class)
    private Account account;

    @ManyToOne(targetEntity = Post.class)
    @JsonSerialize(using = PostSerializer.class)
    private Post post;

    @ManyToOne(targetEntity = PostComment.class)
    @JsonSerialize(using = PostCommentSerializer.class)
    private PostComment postComment;

    private String comment;

    private LocalDateTime regDate;

}
