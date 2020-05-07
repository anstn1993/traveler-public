package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 추가 후 정보 가져오기")
    public void findByEmail() {
        //Given
        String username = "anstn1993@email.com";
        String password = "1111";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        accountService.saveAccount(account);

        //When
        UserDetailsService userDetailsService = accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //Then
        assertThat(passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }

    @Test
    @DisplayName("사용자 추가 후 정보 가져오기 실패")
   public void findByEmailFail() {
        String username = "anstn1993@email.com";
        UsernameNotFoundException usernameNotFoundException = assertThrows(UsernameNotFoundException.class, () -> {
            accountService.loadUserByUsername(username);
        });
        assertThat(usernameNotFoundException.getMessage()).contains(username);
    }

}