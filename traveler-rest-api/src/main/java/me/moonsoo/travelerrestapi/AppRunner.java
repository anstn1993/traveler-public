package me.moonsoo.travelerrestapi;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.IntStream;

//@Component
@Profile("test")
public class AppRunner implements ApplicationRunner {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        followRepository.deleteAll();
        accountRepository.deleteAll();
        Account followingAccount = createAccount(0);
        IntStream.range(1, 100).forEach(i -> {
            Account followedAccount = createAccount(i);
            Follow follow = createFollow(followingAccount, followedAccount);
        });
    }

    private Follow createFollow(Account followingAccount, Account followedAccount) {
        Follow follow = Follow.builder()
                .followingAccount(followingAccount)
                .followedAccount(followedAccount)
                .build();
        return followRepository.save(follow);
    }

    private Account createAccount(int index) {
        Account account = Account.builder()
                .username("user" + index)
                .authCode("authCode")
                .email("user" + index + "@email.com")
                .password(passwordEncoder.encode("11111111"))
                .emailAuth(true)
                .name("김이름")
                .roles(Set.of(AccountRole.USER))
                .nickname("user" + index)
                .profileImageUri(null)
                .introduce(null)
                .regDate(ZonedDateTime.now())
                .sex(Sex.MALE)
                .build();
        return accountRepository.save(account);
    }
}
