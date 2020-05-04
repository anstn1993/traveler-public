package me.moonsoo.traveleroauthserver.common;

import me.moonsoo.traveleroauthserver.account.Account;
import me.moonsoo.traveleroauthserver.account.AccountRole;
import me.moonsoo.traveleroauthserver.account.AccountService;
import me.moonsoo.traveleroauthserver.account.Sex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Set;

//@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    AccountService accountService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account admin = Account.builder()
                .email("user@email.com")
                .password("user")
                .name("user")
                .nickname("user")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();

        accountService.saveAccount(admin);
    }
}
