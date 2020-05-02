package me.moonsoo.travelerrestapi.accompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.AccountService;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.config.AuthServerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Import(AuthServerConfig.class)
class AccompanyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountService accountService;

    @Autowired
    RestTemplate restTemplate;

    @Test
    @DisplayName("동행 구하기 게시물 생성 테스트")
    public void createAccompany() throws Exception {
        //Given
        Account account = Account.builder().build();
        String accessToken = getAuthToken(account);
        Accompany accompany = createAccompany(account, 0);

        mockMvc.perform(post("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("accountId").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("startDate").exists())
                .andExpect(jsonPath("endDate").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists());
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 실패 테스트-401(unauthorized)")
    public void createAccountFail_Unauthorized() throws Exception {
        //Given
        Account account = Account.builder().build();
        Accompany accompany = createAccompany(account, 0);

        mockMvc.perform(post("/api/accompanies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    //Authorization server가 실행된 상태에서 테스트해야 한다.
    @Test
    @DisplayName("oauth서버에서 토큰 발급받기")
    public void getAuthToken() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
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

        //access token get
        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setClientId(clientId);
        resourceDetails.setClientSecret(clientPassword);
        resourceDetails.setUsername(email);
        resourceDetails.setPassword(password);
        resourceDetails.setGrantType("password");
        resourceDetails.setAccessTokenUri("http://localhost:8081/oauth/token");
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails);
        log.info(restTemplate.getAccessToken().toString());
    }



    private Accompany createAccompany(Account account, int index) {
        return Accompany.builder()
                .account(account)
                .title("title" + index)
                .article("article")
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .regDate(LocalDateTime.now())
                .build();
    }

    public String getAuthToken(Account account) throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        account = Account.builder()
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

        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setClientId(clientId);
        resourceDetails.setClientSecret(clientPassword);
        resourceDetails.setUsername(email);
        resourceDetails.setPassword(password);
        resourceDetails.setGrantType("password");
        resourceDetails.setAccessTokenUri("http://localhost:8081/oauth/token");
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails);
        log.info(restTemplate.getAccessToken().toString());
        return restTemplate.getAccessToken().toString();
    }

}