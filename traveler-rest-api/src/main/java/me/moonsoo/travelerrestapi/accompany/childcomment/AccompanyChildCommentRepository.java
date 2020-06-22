package me.moonsoo.travelerrestapi.accompany.childcomment;

import io.lettuce.core.dynamic.annotation.Param;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccompanyChildCommentRepository extends JpaRepository<AccompanyChildComment, Integer> {
    Page<AccompanyChildComment> findAllByAccompanyComment(AccompanyComment comment, Pageable pageable);

    @Modifying
    @Query("delete from accompany_child_comment c where c.accompany = :accompany")
    void deleteAllByAccompany(@Param("accompany") Accompany accompany);

    @Modifying
    @Query("delete from accompany_child_comment c where c.accompanyComment = :accompanyComment")
    void deleteAllByAccompanyComment(@Param("accompanyComment") AccompanyComment accompanyComment);

    Optional<AccompanyChildComment> findByAccount(Account account);

    void deleteByAccount(Account account);
}
