package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    public Like save(Like like) {
        return likeRepository.save(like);
    }

    public Optional<Like> findByAccountAndPost(Account account, Post post) {
        return likeRepository.findByAccountAndPost(account, post);
    }
}
