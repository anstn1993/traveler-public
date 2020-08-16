package me.moonsoo.travelerapplication.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerapplication.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FollowedAccountConverter implements Converter<String, Account> {

    @Autowired
    private AccountService accountService;

    @Override
    public Account convert(String followedAccountId) {
        Optional<Account> accountOpt = accountService.findById(Integer.parseInt(followedAccountId));
        if(accountOpt.isEmpty()) {
            return null;
        }
        return accountOpt.get();
    }
}
