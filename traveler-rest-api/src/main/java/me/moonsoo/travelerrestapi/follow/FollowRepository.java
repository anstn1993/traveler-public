package me.moonsoo.travelerrestapi.follow;

import io.lettuce.core.dynamic.annotation.Param;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    Optional<Follow> findByFollowingAccountAndAndFollowedAccount(Account followingAccount, Account followedAccount);

    Page<Follow> findByFollowingAccount(Account followingAccount, Pageable pageable);

    Page<Follow> findByFollowedAccount(Account followedAccount, Pageable pageable);
}
