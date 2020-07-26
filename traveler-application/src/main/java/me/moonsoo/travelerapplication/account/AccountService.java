package me.moonsoo.travelerapplication.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.EmailAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        Account account = accountOpt.orElseThrow(() -> new UsernameNotFoundException(username));
        if(!account.isEmailAuth()) {
            throw new EmailAuthException("You need to authenticate your email.");
        }
        return new AccountAdapter(account);
    }

    public Optional<Account> findUserByNameAndEmail(String name, String email) {
        return accountRepository.findByNameAndEmail(name, email);
    }

    public Optional<Account> findUsernameAndEmail(String username, String email) {
        return accountRepository.findByUsernameAndEmail(username, email);
    }

    //비밀번호 수정
    public void updatePassword(String username, String password) {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        Account account = accountOpt.get();
        account.setPassword(passwordEncoder.encode(password));
        accountRepository.save(account);
    }
}
