package me.moonsoo.travelerrestapi.accompany;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccompanyRepository extends JpaRepository<Accompany, Integer> {
    Page<Accompany> findAllByAccount_NicknameContains(String nickname, Pageable pageable);

    Page<Accompany> findAllByTitleContains(String search, Pageable pageable);

    Page<Accompany> findAllByArticleContains(String search, Pageable pageable);

    Page<Accompany> findAllByLocationContains(String search, Pageable pageable);
}
