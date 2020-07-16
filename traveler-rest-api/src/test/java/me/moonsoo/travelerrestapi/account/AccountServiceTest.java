package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest extends BaseControllerTest {

    @Autowired
    private AccountAuthService accountAuthService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 추가 후 정보 가져오기")
    public void findByEmail() {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        Account account = Account.builder()
                .username(username)
                .email(email)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(true)
                .authCode("code")
                .profileImageUri(null)
                .sex(Sex.MALE)
                .regDate(LocalDateTime.now())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        accountAuthService.saveAccount(account);

        //When
        UserDetails userDetails = accountAuthService.loadUserByUsername(username);

        //Then
        assertThat(passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }

    @Test
    @DisplayName("사용자 추가 후 정보 가져오기 실패")
   public void findByEmailFail() {
        String username = "anstn1993";
        UsernameNotFoundException usernameNotFoundException = assertThrows(UsernameNotFoundException.class, () -> {
            accountAuthService.loadUserByUsername(username);
        });
        assertThat(usernameNotFoundException.getMessage()).contains(username);
    }

}