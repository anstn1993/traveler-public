package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class PostChildCommentService {

    @Autowired
    private PostChildCommentRepository postChildCommentRepository;

    public PostChildComment save(Post post, PostComment postComment, Account account, PostChildCommentDto postChildCommentDto) {
        PostChildComment postChildComment = PostChildComment.builder()
                .account(account)
                .post(post)
                .postComment(postComment)
                .regDate(ZonedDateTime.now())
                .comment(postChildCommentDto.getComment())
                .build();
        return postChildCommentRepository.save(postChildComment);
    }

    public Page<PostChildComment> findAllByPostComment(PostComment postComment, Pageable pageable) {
        return postChildCommentRepository.findAllByPostComment(postComment, pageable);
    }

    public PostChildComment update(PostChildComment postChildComment, PostChildCommentDto postChildCommentDto) {
        postChildComment.setComment(postChildCommentDto.getComment());
        return postChildCommentRepository.save(postChildComment);
    }

    public void delete(PostChildComment postChildComment) {
        postChildCommentRepository.delete(postChildComment);
    }
}
