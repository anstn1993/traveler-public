package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostChildCommentService {

    @Autowired
    private PostChildCommentRepository postChildCommentRepository;

    public PostChildComment save(Post post, PostComment postComment, Account account, PostChildCommentDto postChildCommentDto) {
        PostChildComment postChildComment = PostChildComment.builder()
                .account(account)
                .post(post)
                .postComment(postComment)
                .regDate(LocalDateTime.now())
                .comment(postChildCommentDto.getComment())
                .build();
        return postChildCommentRepository.save(postChildComment);
    }
}
