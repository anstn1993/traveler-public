package me.moonsoo.travelerrestapi.accompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.moonsoo.commonmodule.account.*;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccompanyControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccompanyRepository accompanyRepository;

    private Account account;

    @BeforeEach
    public void setUp() {
        accompanyRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동행 구하기 게시물 생성 테스트")
    public void createAccompanyDto() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        AccompanyDto accompanyDto = createAccompanyDto(0);

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
                                linkWithRel("get-accompanies").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                linkWithRel("update-accompany").description("업로드된 동행 게시물을 수정할 수 있는 url"),
                                linkWithRel("delete-accompany").description("업로드된 동행 게시물을 삭제할 수 있는 url"),
                                linkWithRel("profile").description("api 문서 url")
                        ),
                        requestHeaders,
                        requestFields(
                                fields.withPath("title").description("동행 게시물의 제목"),
                                fields.withPath("article").description("동행 게시물의 본문"),
                                fields.withPath("startDate").description("동행 시작 시간"),
                                fields.withPath("endDate").description("동행 종료 시간"),
                                fields.withPath("location").description("동행 장소명"),
                                fields.withPath("latitude").description("동행 장소의 위도"),
                                fields.withPath("longitude").description("동행 장소의 경도")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("업로드한 게시물의 리소스 url"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
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
                                fieldWithPath("_links.self.href").description("업로드된 동행 게시물의 리소스 url"),
                                fieldWithPath("_links.get-accompanies.href").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                fieldWithPath("_links.update-accompany.href").description("업로드된 동행 게시물을 수정할 수 있는 url"),
                                fieldWithPath("_links.delete-accompany.href").description("업로드된 동행 게시물을 삭제할 수 있는 url"),
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
        AccompanyDto accompany = createAccompanyDto(0);

        mockMvc.perform(post("/api/accompanies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompany)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("미인증 상태에서 동행게시물 목록 요청 테스트-60개의 게시물, 한 페이지에 10개씩 가져온다고 할 때 두 번째 페이지 가져오기")
    public void getAccompanies_Without_Auth() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(email, password);
        IntStream.range(0, 60).forEach(i -> {
            createAccompany(account, i);
        });


        mockMvc.perform(get("/api/accompanies")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyList").exists())
                .andExpect(jsonPath("_embedded.accompanyList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 동행게시물 목록 요청 테스트(작성자 검색어 적용)-60개의 게시물, 한 페이지에 10개씩 가져온다고 할 때 두 번째 페이지 가져오기")
    public void getAccompanies_With_Auth() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        IntStream.range(0, 30).forEach(i -> {
            createAccompany(account, i);
        });


        mockMvc.perform(get("/api/accompanies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "location")
                .param("search", "some"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyList").exists())
                .andExpect(jsonPath("_embedded.accompanyList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-accompany").exists())
                .andDo(document("get-accompanies",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-accompany").description("동행 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders,
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준"),
                                parameterWithName("filter").optional().description("검색어 필터(writer-작성자, article-본문, title-제목, location-장소명)"),
                                parameterWithName("search").optional().description("검색어")
                        )
                        , responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        )
                        , responseFields(
                                fieldWithPath("_embedded.accompanyList[0].id").description("동행 게시물의 id"),
                                fieldWithPath("_embedded.accompanyList[0].account.id").description("게시물 작성자의 id"),
                                fieldWithPath("_embedded.accompanyList[0].title").description("동행 게시물의 제목"),
                                fieldWithPath("_embedded.accompanyList[0].article").description("동행 게시물의 본문"),
                                fieldWithPath("_embedded.accompanyList[0].startDate").description("동행 시작 시간"),
                                fieldWithPath("_embedded.accompanyList[0].endDate").description("동행 종료 시간"),
                                fieldWithPath("_embedded.accompanyList[0].location").description("동행 장소명"),
                                fieldWithPath("_embedded.accompanyList[0].latitude").description("동행 장소의 위도"),
                                fieldWithPath("_embedded.accompanyList[0].longitude").description("동행 장소의 경도"),
                                fieldWithPath("_embedded.accompanyList[0].regDate").description("동행 게시물 작성 시간"),
                                fieldWithPath("_embedded.accompanyList[0]._links.self.href").description("동행 게시물 리소스 요청 url"),
                                fieldWithPath("_links.first.href").description("첫 번째 페이지 리소스 요청 url"),
                                fieldWithPath("_links.prev.href").description("이전 페이지 리소스 요청 url"),
                                fieldWithPath("_links.self.href").description("현재 페이지 리소스 요청 url"),
                                fieldWithPath("_links.next.href").description("다음 페이지 리소스 요청 url"),
                                fieldWithPath("_links.last.href").description("마지막 페이지 리소스 요청 url"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.create-accompany.href").description("동행 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)"),
                                fieldWithPath("page.size").description("한 페이지에 보여줄 동행 게시물의 수"),
                                fieldWithPath("page.totalElements").description("모든 동행 게시물의 수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.number").description("현재 페이지 번호")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시물 하나 가져오기(미인증 상태)")
    public void getAccompany_Without_Auth() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        account = createAccount(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        mockMvc.perform(get("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
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
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    @DisplayName("동행 게시물 하나 가져오기(인증 상태에서 자신의 게시물을 조회하는 경우)")
    public void getAccompany_With_Auth() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        //pathParameters를 사용하여 문서화흘 하기 위해서 RestDocumentationRequestBuilders.get을 사용
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
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
                .andDo(document("get-accompany",
                        links(
                                linkWithRel("self").description("조회한 동행 게시물의 리소스 url"),
                                linkWithRel("get-accompanies").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                linkWithRel("update-accompany").description("동행 게시물을 수정할 수 있는 url(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                linkWithRel("delete-accompany").description("동행 게시물을 삭제할 수 있는 url(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                linkWithRel("profile").description("api 문서 url")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("id").description("동행 게시물의 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
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
                                fieldWithPath("_links.self.href").description("조회한 동행 게시물의 리소스 url"),
                                fieldWithPath("_links.get-accompanies.href").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                fieldWithPath("_links.update-accompany.href").description("동행 게시물을 수정할 수 있는 url(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-accompany.href").description("동행 게시물을 삭제할 수 있는 url(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 url")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시물 하나 가져오기 실패-404 not found")
    public void getAccompany_Not_Found() throws Exception {
        mockMvc.perform(get("/api/accompanies/404")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 수정")
    public void updateAccompany() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);
        //수정할 데이터
        String title = "updated title";
        String article = "updated article";

        //When
        AccompanyDto accompanyDto = modelMapper.map(savedAccompany, AccompanyDto.class);
        //게시물 수정
        accompanyDto.setTitle(title);
        accompanyDto.setArticle(article);
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("title").value(title))
                .andExpect(jsonPath("article").value(article))
                .andExpect(jsonPath("startDate").exists())
                .andExpect(jsonPath("endDate").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompanies").exists())
                .andExpect(jsonPath("_links.delete-accompany").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-accompany",
                        links(
                                linkWithRel("self").description("조회한 동행 게시물의 리소스 url"),
                                linkWithRel("get-accompanies").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                linkWithRel("delete-accompany").description("동행 게시물을 삭제할 수 있는 url"),
                                linkWithRel("profile").description("api 문서 url")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("id").description("리소스의 id")
                        ),
                        requestFields(
                                fieldWithPath("title").description("동행 게시물의 제목"),
                                fieldWithPath("article").description("동행 게시물의 본문"),
                                fieldWithPath("startDate").description("동행 시작 시간"),
                                fieldWithPath("endDate").description("동행 종료 시간"),
                                fieldWithPath("location").description("동행 장소명"),
                                fieldWithPath("latitude").description("동행 장소의 위도"),
                                fieldWithPath("longitude").description("동행 장소의 경도")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
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
                                fieldWithPath("_links.self.href").description("업로드된 동행 게시물의 리소스 url"),
                                fieldWithPath("_links.get-accompanies.href").description("동행 게시물 리스트를 조회할 수 있는 url"),
                                fieldWithPath("_links.delete-accompany.href").description("업로드된 동행 게시물을 삭제할 수 있는 url"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )))
        ;

    }

    @Test
    @DisplayName("동행 게시물 수정 실패-요청 본문을 전달하지 않은 경우(400 bad request)")
    public void updateAccompanyFail_Empty_Value() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);
        AccompanyDto accompanyDto = AccompanyDto.builder().build();//빈 요청 본문 Dto

        mockMvc.perform(put("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동행 게시물 수정 실패-요청 본문의 값이 잘못된 경우(400 bad request)")
    public void updateAccompanyFail_Wrong_Value() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);
        //유효하지 않은 데이터 조합(시작 날짜보다 종료 날짜가 빠른 경우)
        LocalDateTime startDate = LocalDateTime.of(2020, 5, 5, 12, 12, 12);
        LocalDateTime endDate = LocalDateTime.of(2020, 5, 4, 12, 12, 12);

        //when
        AccompanyDto accompanyDto = modelMapper.map(savedAccompany, AccompanyDto.class);
        accompanyDto.setStartDate(startDate);
        accompanyDto.setEndDate(endDate);
        mockMvc.perform(put("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동행 게시물 수정 실패-존재하지 않는 게시물을 수정하는 경우(404 not found)")
    public void updateAccompany_Not_Found() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        //When
        AccompanyDto accompanyDto = modelMapper.map(savedAccompany, AccompanyDto.class);
        mockMvc.perform(put("/api/accompanies/404")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("동행 게시물 수정 실패-다른 사용자의 게시물을 수정하려고 하는 경우(403 forbidden)")
    public void updateAccompany_Forbidden() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);

        Account otherAccount = Account.builder()
                .email("otheruser@email.com")
                .password("otheruser")
                .name("김랑")
                .nickname("rang")
                .roles(Set.of(AccountRole.USER))
                .sex(Sex.FEMALE)
                .build();
        accountRepository.save(otherAccount);

        //수정할 데이터
        String title = "updated title";
        String article = "updated article";
        Accompany savedAccompany = createAccompany(otherAccount, 0);//다른 사용자가 만든 게시물
        AccompanyDto accompanyDto = modelMapper.map(savedAccompany, AccompanyDto.class);
        //게시물 수정
        accompanyDto.setTitle(title);
        accompanyDto.setArticle(article);
        mockMvc.perform(put("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시물 삭제")
    public void deleteAccompany() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-accompany",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("id").description("리소스 id")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("게시물 삭제 실패-인증하지 않은 경우(401 unauthorized)")
    public void deleteAccompanyFaid_Without_Auth() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        createAccount(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{id}", savedAccompany.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("게시물 삭제 실패-존재하지 않는 게시물(404 not found)")
    public void deleteAccompanyFail_Not_Found() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);
        Accompany savedAccompany = createAccompany(account, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/404")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("게시물 삭제 실패-다른 사용자의 게시물 삭제(403 forbidden)")
    public void deleteAccompanyFail_Forbidden() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password);

        Account otherAccount = Account.builder()
                .email("otheruser@email.com")
                .password("otheruser")
                .name("김랑")
                .nickname("rang")
                .roles(Set.of(AccountRole.USER))
                .sex(Sex.FEMALE)
                .build();
        accountRepository.save(otherAccount);

        Accompany savedAccompany = createAccompany(otherAccount, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{id}", savedAccompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
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

    private AccompanyDto createAccompanyDto(int index) {
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

    private Accompany createAccompany(Account account, int index) {
        Accompany accompany = Accompany.builder()
                .title("title" + index)
                .article("article" + index)
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .account(account)
                .regDate(LocalDateTime.now())
                .build();
        return accompanyRepository.save(accompany);
    }

    //인자로 들어가는 account는 save된 상태
    private String getAuthToken(String email, String password) throws Exception {
        //Given
        String clientId = "traveler";
        String clientPassword = "pass";
        account = createAccount(email, password);

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