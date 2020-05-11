package me.moonsoo.travelerrestapi.accompany.childcomment;

import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccompanyChildCommentRepository extends JpaRepository<AccompanyChildComment, Integer> {
    Page<AccompanyChildComment> findAllByAccompanyComment(AccompanyComment comment, Pageable pageable);
}
