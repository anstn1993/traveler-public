package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    Page<PostComment> findAllByPost(Post post, Pageable pageable);
}
