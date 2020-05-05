package me.moonsoo.traveleroauthserver.common;

import me.moonsoo.commonmodule.account.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account user = Account.builder()
                .email("user@email.com")
                .password("user")
                .name("user")
                .nickname("user")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();

        Account admin = Account.builder()
                .email("admin@email.com")
                .password("admin")
                .name("admin")
                .nickname("admin")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.ADMIN))
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
