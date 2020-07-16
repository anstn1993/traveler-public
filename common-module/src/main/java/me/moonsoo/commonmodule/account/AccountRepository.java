package me.moonsoo.commonmodule.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String username);

    Page<Account> findAllByEmailAuthIsTrue(Pageable pageable);
    Page<Account> findAllByEmailAuthIsTrueAndNameContains(Pageable pageable, String name);
    Page<Account> findAllByEmailAuthIsTrueAndNicknameContains(Pageable pageable, String nickname);
}
