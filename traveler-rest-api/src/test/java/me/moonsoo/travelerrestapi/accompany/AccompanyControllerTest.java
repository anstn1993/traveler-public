package me.moonsoo.travelerrestapi.accompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.account.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccompanyControllerTest extends BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccompanyRepository accompanyRepository;

    @BeforeEach
    public void setUp() {
        accompanyRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 테스트")
    public void createAccompany() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        AccompanyDto accompanyDto = createAccompany(0);

        ConstrainedFields fields = new ConstrainedFields(Accompany.class);

        mockMvc.perform(post("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken(email, password))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("startDate").exists())
                .andExpect(jsonPath("endDate").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompanies").exists())
                .andExpect(jsonPath("_links.update-accompany").exists())
                .andExpect(jsonPath("_links.delete-accompany").exists())
                .andExpect(jsonPath("_links.profile").exists())
                //rest docs 적용
                .andDo(document("create-accompany",
                        links(
                                linkWithRel("self").description("업로드된 동행 게시물의 리소스 링크"),
                                linkWithRel("get-accompanies").description("동행 게시물 리스트를 조회할 수 있는 링크"),
                                linkWithRel("update-accompany").description("업로드된 동행 게시물을 수정할 수 있는 링크"),
                                linkWithRel("delete-accompany").description("업로드된 동행 게시물을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 컨텐츠 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        requestFields(
                                fields.withPath("title").description("동행 게시물의 제목"),
                                fields.withPath("article").description("동행 게시물의 본문"),
                                fields.withPath("startDate").description("동행 시작 시간"),
                                fields.withPath("endDate").description("동행 종료 시간"),
                                fields.withPath("location").description("동행 장소명"),
                                fields.withPath("latitude").description("동행 장소의 위도"),
                                fields.withPath("longitude").description("동행 장소의 경도")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("업로드한 게시물의 리소스 url"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문의 컨텐츠 타입")
                        ),
                        responseFields(
                                fieldWithPath("id").description("동행 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자의 id"),
                                fieldWithPath("title").description("동행 게시물의 제목"),
                                fieldWithPath("article").description("동행 게시물의 본문"),
                                fieldWithPath("startDate").description("동행 시작 시간"),
                                fieldWithPath("endDate").description("동행 종료 시간"),
                                fieldWithPath("location").description("동행 장소명"),
                                fieldWithPath("latitude").description("동행 장소의 위도"),
                                fieldWithPath("longitude").description("동행 장소의 경도"),
                                fieldWithPath("regDate").description("동행 게시물 작성 시간"),
                                fieldWithPath("_links.self.href").description("업로드된 동행 게시물의 리소스 링크"),
                                fieldWithPath("_links.get-accompanies.href").description("동행 게시물 리스트를 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-accompany.href").description("업로드된 동행 게시물을 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-accompany.href").description("업로드된 동행 게시물을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 실패 테스트-요청 본문 값이 담기지 않은 경우 400(bad request)")
    public void createAccompanyFail_BadRequest_Empty_Value() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        AccompanyDto accompanyDto = AccompanyDto.builder().build();//값이 모두 빈 객체

        mockMvc.perform(post("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken(email, password))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].field").exists())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 실패 테스트-요청 본문에 허용되지 않은 값이 담기는 경우 400(bad request)")
    public void createAccompanyFail_BadRequest_Unknown_Property() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";

        //허용되지 않은 값은 id, regDate
        Accompany accompany = Accompany.builder()
                .id(2)
                .title("title")
                .article("article")
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .regDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken(email, password))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 실패 테스트-요청 본문의 값이 비즈니스 로직에 맞지 않은 경우 400(bad request)")
    public void createAccompanyFail_BadRequest_Wrong_Value() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";

        //startDate가 endDate보다 늦는 경우
        AccompanyDto accompany = AccompanyDto.builder()
                .title("title")
                .article("article")
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 26, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .build();

        mockMvc.perform(post("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken(email, password))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].field").exists())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }


    @Test
    @DisplayName("동행 구하기 게시물 생성 실패 테스트-401(unauthorized)")
    public void createAccompanyFail_Unauthorized() throws Exception {
        //Given
        AccompanyDto accompany = createAccompany(0);

        mockMvc.perform(post("/api/accompanies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    private Account createAccount(String email, String password) {
        //Given
        Account account = Account.builder()
                .email(email)
                .password(password)
                .name("김문수")
                .nickname("만수")
                .emailAuth(false)
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();
        return accountService.saveAccount(account);
    }

    private AccompanyDto createAccompany(int index) {
        return AccompanyDto.builder()
                .title("title" + index)
                .article("article")
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .build();
    }

    //인자로 들어가는 account는 save된 상태
    private String getAuthToken(String email, String password) throws Exception {
        //Given
        String clientId = "traveler";
        String clientPassword = "pass";
        createAccount(email, password);

        String contentAsString = mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", email)
                .param("password", password)
                .param("grant_type", "password"))
                .andReturn().getResponse().getContentAsString();

        Jackson2JsonParser parser = new Jackson2JsonParser();
        return (String) parser.parseMap(contentAsString).get("access_token");
    }

    private class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        public ConstrainedFields(Class<?> domain) {
            this.constraintDescriptions = new ConstraintDescriptions(domain);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path)
                    .attributes(key("constraints")
                            .value(StringUtils.collectionToCommaDelimitedString(this.constraintDescriptions.descriptionsForProperty(path))));
        }
    }
}