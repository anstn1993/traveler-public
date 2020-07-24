package me.moonsoo.travelerapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerapplication.main.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Import({SecurityConfig.class})
@TestPropertySource({"classpath:/application-test.properties"})
@Disabled
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected AccountAuthService accountAuthService;

    protected Account account;

    @Autowired
    protected ModelMapper modelMapper;



    //계정 생성
    protected Account createAccount(String username, String email, String password, int index) {
        //Given
        Account account = Account.builder()
                .username(username)
                .email(index + email)
                .password(password)
                .name("user" + index)
                .nickname("user" + index)
                .emailAuth(true)
                .profileImageUri(null)
                .regDate(LocalDateTime.now())
                .authCode("authcode")
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();
        return accountAuthService.saveAccount(account);
    }

    //인자로 들어가는 account는 save된 상태
    protected String getAuthToken(String username, String email, String password, int index) throws Exception {
        //Given
        String clientId = "traveler";
        String clientPassword = "pass";
        account = createAccount(username, email, password, index);

        String contentAsString = mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", index + email)
                .param("password", password)
                .param("grant_type", "password"))
                .andReturn().getResponse().getContentAsString();

        Jackson2JsonParser parser = new Jackson2JsonParser();
        return (String) parser.parseMap(contentAsString).get("access_token");
    }


}
