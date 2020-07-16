package me.moonsoo.travelerapplication.main.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.EmailAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        Account account = accountOpt.orElseThrow(() -> new UsernameNotFoundException(username));
        if(!account.isEmailAuth()) {
            throw new EmailAuthException("You need to authenticate your email.");
        }
        return new AccountAdapter(account);
    }
}
