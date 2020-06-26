package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.beans.factory.annotation.Autowired;
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
}
