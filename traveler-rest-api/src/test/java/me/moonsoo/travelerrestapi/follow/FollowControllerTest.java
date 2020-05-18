package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FollowControllerTest extends BaseControllerTest {

    @Autowired
    FollowRepository followRepository;

    @AfterEach
    public void setUp() {
        followRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("특정 사용자 팔로우")
    public void followUser() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account targetAccount = createAccount(email, password, 1);//팔로우를 당할 사용자 추가

        FollowDto followDto = createFollowDto(targetAccount);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(followDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("followingAccount.id").exists())
                .andExpect(jsonPath("followingAccount.email").exists())
                .andExpect(jsonPath("followingAccount.name").exists())
                .andExpect(jsonPath("followingAccount.nickname").exists())
                .andExpect(jsonPath("followingAccount.profileImagePath").hasJsonPath())
                .andExpect(jsonPath("followingAccount.sex").exists())
                .andExpect(jsonPath("followedAccount.id").exists())
                .andExpect(jsonPath("followedAccount.email").exists())
                .andExpect(jsonPath("followedAccount.name").exists())
                .andExpect(jsonPath("followedAccount.nickname").exists())
                .andExpect(jsonPath("followedAccount.profileImagePath").hasJsonPath())
                .andExpect(jsonPath("followedAccount.sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-account-followings").exists())
                .andExpect(jsonPath("_links.get-account-followers").exists())
                .andExpect(jsonPath("_links.delete-account-following").exists())
        .andDo(document("create-follow",
                links(
                        linkWithRel("self").description("팔로우한 사용자 정보 조회 링크"),
                        linkWithRel("profile").description("api 문서 링크"),
                        linkWithRel("get-account-followings").description("사용자 팔로잉 목록 조회 링크"),
                        linkWithRel("get-account-followers").description("사용자 팔로워 목록 조회 링크"),
                        linkWithRel("delete-account-following").description("팔로우한 사용자 언팔로우 링크")
                ),
                requestHeaders,
                pathParameters(
                        parameterWithName("accountId").description("팔로우를 하는 주체가 되는 사용자의 id(요청을 보내는 사용자 자신의 id)")
                ),
                requestFields(
                        fieldWithPath("followedAccount.id").description("팔로우의 대상이 되는 사용자 id")
                ),
                responseFields(
                        fieldWithPath("id").description("팔로우 리소스 id"),
                        fieldWithPath("followingAccount.id").description("팔로우하는 사용자의 id(사용자 자신의 id)"),
                        fieldWithPath("followingAccount.email").description("팔로우하는 사용자의 email"),
                        fieldWithPath("followingAccount.name").description("팔로우하는 사용자의 이름"),
                        fieldWithPath("followingAccount.nickname").description("팔로우하는 사용자의 닉네임"),
                        fieldWithPath("followingAccount.profileImagePath").description("팔로우하는 사용자의 프로필 이미지 경로"),
                        fieldWithPath("followingAccount.sex").description("팔로우하는 사용자의 성별"),
                        fieldWithPath("followedAccount.id").description("팔로우의 대상이 되는 사용자의 id"),
                        fieldWithPath("followedAccount.email").description("팔로우의 대상이 되는 사용자의 이메일"),
                        fieldWithPath("followedAccount.name").description("팔로우의 대상이 되는 사용자의 이름"),
                        fieldWithPath("followedAccount.nickname").description("팔로우의 대상이 되는 사용자의 닉네임"),
                        fieldWithPath("followedAccount.profileImagePath").description("팔로우의 대상이 되는 사용자의 프로필 이미지 경로"),
                        fieldWithPath("followedAccount.sex").description("팔로우의 대상이 되는 사용자의 성별"),
                        fieldWithPath("_links.self.href").description("팔로우의 대상이 되는 사용자 정보 조회 링크"),
                        fieldWithPath("_links.profile.href").description("api 문서 링크"),
                        fieldWithPath("_links.get-account-followings.href").description("사용자 팔로잉 목록 조회 링크"),
                        fieldWithPath("_links.get-account-followers.href").description("사용자 팔로워 목록 조회 링크"),
                        fieldWithPath("_links.delete-account-following.href").description("팔로우한 사용자 언팔로우 링크")
                )
                ))
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-요청 본문이 없는 경우(400 Bad request)")
    public void followUserFail_Empty_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-요청 본문에 허용되지 않은 값이 포함된 경우(400 Bad request)")
    public void followUserFail_Wrong_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account targetAccount = createAccount(email, password, 1);//팔로우를 당할 사용자 추가
        Follow follow = Follow.builder()
                .followedAccount(targetAccount)
                .followingAccount(account)
                .build();

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(follow)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-인증을 하지 않은 경우(401 Unauthorized)")
    public void followUserFail_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Account targetAccount = createAccount(email, password, 1);//팔로우를 당할 사용자 추가
        FollowDto followDto = createFollowDto(targetAccount);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(followDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-다른 사용자로 팔로우 요청을 보내는 경우(403 Forbidden)")
    public void followUserFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account targetAccount = createAccount(email, password, 1);//팔로우를 당할 사용자 추가
        Account otherAccount = createAccount(email, password, 2);//다른 사용자
        FollowDto followDto = createFollowDto(targetAccount);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", otherAccount.getId()
        )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(followDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-자기 자신을 팔로우 하는 경우(400 Bad request)")
    public void followUserFail_Forbidden_Self_Follow() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        FollowDto followDto = createFollowDto(account);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(followDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("특정 사용자 팔로우 실패-이미 팔로우 중인 사용자를 팔로우하는 경우(400 Bad request)")
    public void followUserFail_Already_Following() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account followedAccount = createAccount(email, password, 1);
        createFollow(account, followedAccount);//account가 followedAccount를 follow

        FollowDto followDto = createFollowDto(followedAccount);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(followDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 특정 사용자의 팔로잉 목록 조회(한 페이지에 10개씩, 1페이지 조회, id를 기준으로 오름차순)")
    public void getFollowings_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

//        사용자 10개 추가 후 10명 팔로우
        IntStream.rangeClosed(1, 30).forEach(i -> {
            Account followedAccount = createAccount(email, password, i);
            Follow follow = createFollow(account, followedAccount);
        });
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("size", "10")
                .param("page", "1")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.delete-account-follow").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.profile").exists())
        .andDo(document("get-account-followings",
                pagingLinks.and(
                        linkWithRel("profile").description("api 문서 링크")
                ),
                requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                        headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                ),
                pathParameters(
                        parameterWithName("accountId").description("팔로잉 목록 소유자 id")
                ),
                requestParameters(
                        parameterWithName("page").optional().description("페이지 번호"),
                        parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                        parameterWithName("sort").optional().description("정렬 기준(id)")
                ),
                responseHeaders,
                responsePageFields.and(
                        fieldWithPath("_embedded.accountList[0].id").description("사용자의 id"),
                        fieldWithPath("_embedded.accountList[0].email").description("사용자의 email"),
                        fieldWithPath("_embedded.accountList[0].nickname").description("사용자의 닉네임"),
                        fieldWithPath("_embedded.accountList[0].name").description("사용자의 이미지"),
                        fieldWithPath("_embedded.accountList[0].profileImagePath").description("사용자의 프로필 사진 경로"),
                        fieldWithPath("_embedded.accountList[0].sex").description("사용자의 성별"),
                        fieldWithPath("_embedded.accountList[0]._links.self.href").description("해당 사용자 정보 조회 링크"),
                        fieldWithPath("_embedded.accountList[0]._links.delete-account-follow.href").description("해당 사용자 unfollow 링크, 만약 팔로우 상태인 경우에는 팔로우 링크가 제공된다.(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 특정 사용자의 팔로잉 목록 조회(한 페이지에 10개씩, 1페이지 조회, id를 기준으로 오름차순)")
    public void getFollowings_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //사용자 30개 추가 후 30명 팔로우
        IntStream.rangeClosed(1, 30).forEach(i -> {
            Account followedAccount = createAccount(email, password, i);
            createFollow(account, followedAccount);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}/followings", account.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .param("size", "10")
                .param("page", "1")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.delete-account-follow").doesNotExist())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    public void getOneFollow() {
        String email = "anstn1993@email.com";
        String password = "1111";
        Account followingAccount = createAccount(email, password, 0);
        Account followedAccount = createAccount(email, password, 1);
        createFollow(followingAccount, followedAccount);
        Optional<Follow> followOpt = followRepository.findByFollowingAccountAndAndFollowedAccount(followingAccount, followedAccount);
        Follow follow = followOpt.orElseThrow(() -> new NullPointerException("null"));
        assertThat(follow.getFollowingAccount().getEmail()).isEqualTo(followingAccount.getEmail());
        assertThat(follow.getFollowedAccount().getEmail()).isEqualTo(followedAccount.getEmail());

    }

    private FollowDto createFollowDto(Account targetAccount) {
        return FollowDto.builder()
                .followedAccount(targetAccount)
                .build();
    }

    private Follow createFollow(Account followingAccount, Account followedAccount) {
        Follow follow = Follow.builder()
                .followingAccount(followingAccount)
                .followedAccount(followedAccount)
                .build();

        return followRepository.save(follow);
    }

}