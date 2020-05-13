package me.moonsoo.commonmodule.account;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.util.Optional;

public class AccountFormatter implements Converter<String, Account> {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public Account convert(String accountIdStr) {
        Integer accountId = Integer.parseInt(accountIdStr);
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if(accountOpt.isEmpty()) {
            return null;
        }
        return accountOpt.get();
    }
}
