package me.moonsoo.travelerapplication.account;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountConverter implements Converter<String, Account> {

    @Autowired
    private AccountService accountService;

    @Override
    public Account convert(String userId) {
        Optional<Account> accountOpt = accountService.findById(Integer.parseInt(userId));
        if(accountOpt.isEmpty()) {
            return null;
        }
        return accountOpt.get();
    }
}
