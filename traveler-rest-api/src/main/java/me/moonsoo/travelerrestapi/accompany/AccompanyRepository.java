package me.moonsoo.travelerrestapi.accompany;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccompanyRepository extends JpaRepository<Accompany, Integer> {
    Page<Accompany> findAllByAccount_NicknameContains(String nickname, Pageable pageable);

    Page<Accompany> findAllByTitleContains(String search, Pageable pageable);

    Page<Accompany> findAllByArticleContains(String search, Pageable pageable);

    Page<Accompany> findAllByLocationContains(String search, Pageable pageable);

    Optional<Accompany> findByAccount(Account account);

    void deleteByAccount(Account account);
}
