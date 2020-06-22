package me.moonsoo.travelerrestapi.accompany.comment;

import io.lettuce.core.dynamic.annotation.Param;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccompanyCommentRepository extends JpaRepository<AccompanyComment, Integer> {
    Page<AccompanyComment> findAllByAccompany(Accompany accompany, Pageable pageable);

    @Modifying
    @Query("delete from accompany_comment c where c.accompany = :accompany ")
    void deleteAllByAccompany(@Param("accompany") Accompany accompany);

    Optional<AccompanyComment> findByAccount(Account account);

    void deleteByAccount(Account account);
}
