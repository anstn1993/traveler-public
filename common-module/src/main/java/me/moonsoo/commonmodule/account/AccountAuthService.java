package me.moonsoo.commonmodule.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@Slf4j
public class AccountAuthService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Account saveAccount(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setAuthCode(passwordEncoder.encode(account.getEmail()));
        return accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, EmailAuthException {
        Optional<Account> accountOpt = accountRepository.findByEmail(username);
        Account account = accountOpt.orElseThrow(() -> new UsernameNotFoundException(username));
        if(!account.isEmailAuth()) {
            throw new EmailAuthException("You need to authenticate your email.");
        }
        return new AccountAdapter(account);
    }
}

