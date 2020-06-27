package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    public PostComment save(Account account, Post post, PostComment postComment) {
        postComment.setAccount(account);
        postComment.setPost(post);
        postComment.setRegDate(LocalDateTime.now());

        return postCommentRepository.save(postComment);
    }

    public Page<PostComment> findAllByPost(Post post, Pageable pageable) {
        return postCommentRepository.findAllByPost(post, pageable);
    }

    public PostComment update(PostComment postComment, PostCommentDto postCommentDto) {
        postComment.setComment(postCommentDto.getComment());
        return postCommentRepository.save(postComment);
    }
}
