package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostChildCommentRepository extends JpaRepository<PostChildComment, Integer> {
    Page<PostChildComment> findAllByPostComment(PostComment postComment, Pageable pageable);

    void deleteByPost(Post post);

    void deleteByPostComment(PostComment postComment);

    void deleteByAccount(Account account);

    Optional<PostChildComment> findByAccount(Account account);
}
