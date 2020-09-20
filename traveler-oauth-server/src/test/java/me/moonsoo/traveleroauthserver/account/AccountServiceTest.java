package me.moonsoo.traveleroauthserver.account;

import me.moonsoo.commonmodule.account.*;
import me.moonsoo.traveleroauthserver.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest extends BaseControllerTest {

    @Autowired
    AccountAuthService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 추가 후 정보 가져오기")
    public void findByEmail() {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String username = "anstn1993";
        Account account = Account.builder()
                .username(username)
                .email(email)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(true)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .regDate(ZonedDateTime.now())
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