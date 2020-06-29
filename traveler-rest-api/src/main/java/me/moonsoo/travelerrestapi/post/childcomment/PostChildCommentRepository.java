package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.travelerrestapi.post.comment.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostChildCommentRepository extends JpaRepository<PostChildComment, Integer> {
    Page<PostChildComment> findAllByPostComment(PostComment postComment, Pageable pageable);
}
