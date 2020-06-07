package me.moonsoo.travelerrestapi;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerrestapi.config.*;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Import({AuthServerConfig.class, ResourceServerConfig.class, SecurityConfig.class, RestDocsConfig.class, MockS3Config.class})
@Disabled
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected AccountService accountService;

    protected Account account;

    //페이징 링크 조각
    protected final LinksSnippet pagingLinks = links(
            linkWithRel("first").description("첫 번째 페이지 리소스 요청 url"),
            linkWithRel("prev").description("이전 페이지 리소스 요청 url"),
            linkWithRel("self").description("현재 페이지 리소스 요청 url"),
            linkWithRel("next").description("다음 페이지 리소스 요청 url"),
            linkWithRel("last").description("마지막 페이지 리소스 요청 url"));

    //request headers 조각
    protected final RequestHeadersSnippet requestHeaders = requestHeaders(
            headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 컨텐츠 타입"),
            headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"));

    protected final ResponseHeadersSnippet responseHeaders = responseHeaders(
            headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문의 컨텐츠 타입"));

    protected final ResponseFieldsSnippet responsePageFields = responseFields(
            fieldWithPath("_links.first.href").description("첫 번째 페이지 리소스 요청 url"),
            fieldWithPath("_links.prev.href").description("이전 페이지 리소스 요청 url"),
            fieldWithPath("_links.self.href").description("현재 페이지 리소스 요청 url"),
            fieldWithPath("_links.next.href").description("다음 페이지 리소스 요청 url"),
            fieldWithPath("_links.last.href").description("마지막 페이지 리소스 요청 url"),
            fieldWithPath("_links.profile.href").description("api 문서 링크"),
            fieldWithPath("page.size").description("한 페이지에 보여줄 아이템의 수"),
            fieldWithPath("page.totalElements").description("모든 아이템의 수"),
            fieldWithPath("page.totalPages").description("전체 페이지 수"),
            fieldWithPath("page.number").description("현재 페이지 번호")
    );

    //계정 생성
    protected Account createAccount(String email, String password, int index) {
        //Given
        Account account = Account.builder()
                .email(index + email)
                .password(password)
                .name("user" + index)
                .nickname("user" + index)
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();
        return accountService.saveAccount(account);
    }

    //인자로 들어가는 account는 save된 상태
    protected String getAuthToken(String email, String password, int index) throws Exception {
        //Given
        String clientId = "traveler";
        String clientPassword = "pass";
        account = createAccount(email, password, index);

        String contentAsString = mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", index + email)
                .param("password", password)
                .param("grant_type", "password"))
                .andReturn().getResponse().getContentAsString();

        Jackson2JsonParser parser = new Jackson2JsonParser();
        return (String) parser.parseMap(contentAsString).get("access_token");
    }


}
