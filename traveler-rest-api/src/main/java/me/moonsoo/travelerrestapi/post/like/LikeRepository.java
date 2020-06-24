package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByAccountAndPost(Account account, Post post);
}
