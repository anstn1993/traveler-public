package me.moonsoo.traveleroauthserver.config;

import me.moonsoo.commonmodule.account.*;
import me.moonsoo.traveleroauthserver.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({AuthServerConfig.class, SecurityConfig.class})
class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        //Given
        String email = "anstn1993@gmail.com";
        String password = "1111";
        Account account = Account.builder()
                .email(email)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();

        accountService.saveAccount(account);

        String clientId = "traveler";
        String clientPassword = "pass";

        mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", email)
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
                .param("username", "notexist@emial.com")
                .param("password", "notexist")
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}