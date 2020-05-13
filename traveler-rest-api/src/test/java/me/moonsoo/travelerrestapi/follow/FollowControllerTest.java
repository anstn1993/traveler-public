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

import java.util.stream.IntStream;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
    @DisplayName("특정 사용자의 팔로잉 목록 조회(한 페이지에 10개씩, 1페이지 조회, id를 기준으로 오름차순)")
    public void getFollowings() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        //사용자 100개 추가 후 100명 팔로우
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Account followedAccount = createAccount(email, password, i);
            createFollow(account, followedAccount);
        });

//        mockMvc.perform
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