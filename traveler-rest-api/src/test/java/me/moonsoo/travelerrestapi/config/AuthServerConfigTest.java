package me.moonsoo.travelerrestapi.config;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
////@ActiveProfiles("test")
//@Slf4j
//@Import({AuthServerConfig.class, ResourceServerConfig.class, SecurityConfig.class})
class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    private AccountAuthService accountAuthService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@gmail.com";
        String password = "1111";
        Account account = Account.builder()
                .username(username)
                .email(email)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(true)
                .authCode("authcode")
                .regDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();

        accountAuthService.saveAccount(account);

        String clientId = "traveler";
        String clientPassword = "pass";

        mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }



    @Test
    @DisplayName("인증 토큰 발급 실패 테스트")
    public void getAuthToken_BadRequest() throws Exception {
        String clientId = "traveler";
        String clientPassword = "pass";

        mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", "notexist")
                .param("password", "notexist")
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}