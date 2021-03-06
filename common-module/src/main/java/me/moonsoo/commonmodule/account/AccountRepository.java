package me.moonsoo.commonmodule.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Page<Account> findAllByEmailAuthIsTrue(Pageable pageable);
    Page<Account> findAllByEmailAuthIsTrueAndNameContains(Pageable pageable, String name);
    Page<Account> findAllByEmailAuthIsTrueAndNicknameContains(Pageable pageable, String nickname);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByNameAndEmail(String name, String email);
    Optional<Account> findByUsernameAndEmail(String username, String email);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByNickname(String nickname);

}
