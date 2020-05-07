package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.travelerrestapi.accompany.Accompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Page<Comment> findAllByAccompany(Accompany accompany, Pageable pageable);
}
