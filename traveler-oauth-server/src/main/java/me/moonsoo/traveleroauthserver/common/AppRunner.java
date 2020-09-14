package me.moonsoo.traveleroauthserver.common;

import me.moonsoo.commonmodule.account.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

//@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    AccountAuthService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account user = Account.builder()
                .username("user")
                .email("user@email.com")
                .password("user")
                .name("user")
                .nickname("user")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .authCode("salkdjflakf")
                .regDate(ZonedDateTime.now())
                .build();

        Account admin = Account.builder()
                .username("admin")
                .email("admin@email.com")
                .password("admin")
                .name("admin")
                .nickname("admin")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.ADMIN))
                .authCode("salkdjflakf")
                .regDate(ZonedDateTime.now())
                .build();

        Optional<Account> userOpt = accountRepository.findByEmail(user.getEmail());
        if(userOpt.isEmpty()) {
            accountService.saveAccount(user);
        }
        Optional<Account> adminOpt = accountRepository.findByEmail(admin.getEmail());
        if(adminOpt.isEmpty()) {
            accountService.saveAccount(admin);
        }
    }
}
