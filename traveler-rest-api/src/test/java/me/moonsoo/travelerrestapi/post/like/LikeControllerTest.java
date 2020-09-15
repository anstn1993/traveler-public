package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest extends PostBaseControllerTest {

    @Autowired
    private LikeRepository likeRepository;

    @AfterEach
    public void tearDown() {
        postImageRepository.deleteAll();
        postTagRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("좋아요 리소스 추가")
    public void createLike() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-likes").exists())
                .andExpect(jsonPath("_links.delete-like").exists())
                .andDo(document("create-like",
                        links(
                                linkWithRel("self").description("추가된 좋아요 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-likes").description("좋아요 리소스 목록을 조회할 수 있는 링크"),
                                linkWithRel("delete-like").description("추가된 좋아요 리소스를 삭제할 수 있는 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName("X-Forwarded-Proto").description("서버의 프로토콜"),
                                headerWithName("X-Forwarded-Host").description("서버의 호스트 주소"),
                                headerWithName("X-Forwarded-Port").description("서버의 포트 번호")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("좋아요 추가할 post 게시물 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("추가한 좋아요 리소스 링크"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("좋아요 리소스 id"),
                                fieldWithPath("account.id").description("좋아요 리소스를 추가한 사용자 id"),
                                fieldWithPath("account.nickname").description("좋아요 리소스를 추가한 사용자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("좋아요 리소스를 추가한 사용자 프로필 이미지 경로"),
                                fieldWithPath("post.id").description("좋아요가 달린 post 게시물 id"),
                                fieldWithPath("_links.self.href").description("추가된 좋아요 리소스 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.get-likes.href").description("좋아요 리소스 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-like.href").description("추가된 좋아요 리소스를 삭제할 수 있는 링크")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("좋아요 리소스 추가 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void createLikeFail_Unauthorized() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);
        Post post = createPost(account, 0, 1, 1);

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 추가 실패-존재하지 않는 post 리소스인 경우(404 Not found)")
    public void createLikeFail_Not_Found() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        mockMvc.perform(post("/api/posts/{postId}/likes", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 추가 실패-이미 좋아요를 한 게시물인 경우(409 Conflict)")
    public void createLikeFail_Conflict() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        createLike(post, account);//좋아요 리소스 생성

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("인증 상태에서 좋아요 리소스 목록 조회")
    public void getLikes_With_Auth() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        //좋아요 리소스 30개 생성
        IntStream.rangeClosed(1, 30).forEach(i -> {
            Account account = createAccount(username, email, password, i);
            createLike(post, account);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes", post.getId())
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.likeList").exists())
                .andExpect(jsonPath("_embedded.likeList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.likeList[0]._links.create-account-follow").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andDo(document("get-likes",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName("X-Forwarded-Proto").description("서버의 프로토콜"),
                                headerWithName("X-Forwarded-Host").description("서버의 호스트 주소"),
                                headerWithName("X-Forwarded-Port").description("서버의 포트 번호")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("좋아요가 달린 게시물 id")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id)")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responsePageFields.and(
                                fieldWithPath("_embedded.likeList[].id").description("좋아요 리소스 id"),
                                fieldWithPath("_embedded.likeList[].account.id").description("좋아요 리소스의 주인 id"),
                                fieldWithPath("_embedded.likeList[].account.nickname").description("좋아요 리소스의 주인 닉네임"),
                                fieldWithPath("_embedded.likeList[].account.profileImageUri").description("좋아요 리소스의 주인 프로필 이미지 경로"),
                                fieldWithPath("_embedded.likeList[].post.id").description("좋아요가 달린 post 게시물의 id"),
                                fieldWithPath("_embedded.likeList[]._links.self.href").description("좋아요 리소스 조회 링크"),
                                fieldWithPath("_embedded.likeList[]._links.create-account-follow.href").description("좋아요 리소스의 주인 팔로우 링크, 만약 이미 팔로우 상태인 경우에는 언팔로우 링크가 제공된다.(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 좋아요 리소스 목록 조회")
    public void getLikes_Without_Auth() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        //좋아요 리소스 30개 생성
        IntStream.rangeClosed(1, 30).forEach(i -> {
            Account account = createAccount(username, email, password, i);
            createLike(post, account);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes", post.getId())
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.likeList").exists())
                .andExpect(jsonPath("_embedded.likeList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.likeList[0]._links.create-account-follow").doesNotExist())
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
    @DisplayName("인증 상태에서 자신의 좋아요 리소스 하나 조회")
    public void getMyLike_With_Auth() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, account);//post 게시물에 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", post.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-account-follow").doesNotExist())
                .andExpect(jsonPath("_links.get-likes").exists())
                .andExpect(jsonPath("_links.delete-like").exists())
                .andDo(document("get-my-like",
                        links(
                                linkWithRel("self").description("조회한 좋아요 리소스 조회 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-likes").description("좋아요 리소스 목록 조회 링크"),
                                linkWithRel("delete-like").description("좋아요 리소스 삭제 링크(인증상태에서 자신의 좋아요 리소스를 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName("X-Forwarded-Proto").description("서버의 프로토콜"),
                                headerWithName("X-Forwarded-Host").description("서버의 호스트 주소"),
                                headerWithName("X-Forwarded-Port").description("서버의 포트 번호")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post게시물 리소스 id"),
                                parameterWithName("likeId").description("좋아요 리소스 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("조회한 좋아요 리소스 id"),
                                fieldWithPath("account.id").description("좋아요 리소스의 주인 id"),
                                fieldWithPath("account.nickname").description("좋아요 리소스의 주인 닉네임"),
                                fieldWithPath("account.profileImageUri").description("좋아요 리소스의 주인 프로필 이미지 경로"),
                                fieldWithPath("post.id").description("좋아요가 달린 post 게시물 리소스 id"),
                                fieldWithPath("_links.self.href").description("조회한 좋아요 리소스 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.get-likes.href").description("좋아요 리소스 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-like.href").description("좋아요 리소스 삭제 링크(인증상태에서 자신의 좋아요 리소스를 조회한 경우에 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 다른 사용자의 좋아요 리소스 하나 조회")
    public void getOthersLike_With_Auth() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, otherAccount);//post 게시물에 다른 사용자의 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", post.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-account-follow").exists())
                .andExpect(jsonPath("_links.get-likes").exists())
                .andExpect(jsonPath("_links.delete-like").doesNotExist())
                .andDo(document("get-other-like",
                        links(
                                linkWithRel("self").description("조회한 좋아요 리소스 조회 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-likes").description("좋아요 리소스 목록 조회 링크"),
                                linkWithRel("create-account-follow").description("좋아요 리소스의 주인 팔로우 링크, 만약 팔로우 상태인 경우에는 언팔로우 링크가 제공된다.(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName("X-Forwarded-Proto").description("서버의 프로토콜"),
                                headerWithName("X-Forwarded-Host").description("서버의 호스트 주소"),
                                headerWithName("X-Forwarded-Port").description("서버의 포트 번호")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post게시물 리소스 id"),
                                parameterWithName("likeId").description("좋아요 리소스 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("조회한 좋아요 리소스 id"),
                                fieldWithPath("account.id").description("좋아요 리소스의 주인 id"),
                                fieldWithPath("account.nickname").description("좋아요 리소스의 주인 닉네임"),
                                fieldWithPath("account.profileImageUri").description("좋아요 리소스의 주인 프로필 이미지 경로"),
                                fieldWithPath("post.id").description("좋아요가 달린 post 게시물 리소스 id"),
                                fieldWithPath("_links.self.href").description("조회한 좋아요 리소스 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.get-likes.href").description("좋아요 리소스 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.create-account-follow.href").description("좋아요 리소스의 주인 팔로우 링크, 만약 팔로우 상태인 경우에는 언팔로우 링크가 제공된다.(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 좋아요 리소스 하나 조회")
    public void getLike_Without_Auth() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, account);//post 게시물에 다른 사용자의 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", post.getId(), like.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-account-follow").doesNotExist())
                .andExpect(jsonPath("_links.get-likes").exists())
                .andExpect(jsonPath("_links.delete-like").doesNotExist())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 하나 조회 실패-존재하지 않는 게시물 리소스(404 Not found)")
    public void getLikeFail_Not_Found_Post() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, account);//post 게시물에 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", 404, like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 하나 조회 실패-post게시물의 자식 like 리소스가 아닌 경우(409 Conflict)")
    public void getLikeFail_Conflict() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Account otherAccount = createAccount(username, email, password, 1);
        Post post1 = createPost(account, 0, 1, 1);//post 리소스 추가
        Post post2 = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post1, otherAccount);//post 게시물에 다른 사용자의 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", post2.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 하나 조회 실패-존재하지 않는 좋아요 리소스(404 Not found)")
    public void getLikeFail_Not_Found_Like() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/likes/{likeId}", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header("X-Forwarded-Proto", proto)
                .header("X-Forwarded-Host", host)
                .header("X-Forwarded-Port", port))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 삭제")
    public void deleteLike() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, account);//post 게시물에 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/likes/{likeId}", post.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
        .andDo(document("delete-like",
                requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                ),
                pathParameters(
                        parameterWithName("postId").description("삭제할 좋아요 리소스의 부모 post 게시물 리소스 id"),
                        parameterWithName("likeId").description("삭제할 좋아요 리소스의 id")
                )
                ))
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 삭제 실패-다른 사용자의 리소스를 제거하려고 하는 경우(403 Forbidden)")
    public void deleteLikeFail_Forbidden() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, otherAccount);//post 게시물에 다른 사용자의 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/likes/{likeId}", post.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 삭제 실패-존재하지 않는 post 리소스(404 Not found)")
    public void deleteLikeFail_Not_Found_Post() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post, account);//post 게시물에 다른 사용자의 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/likes/{likeId}", 404, like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 삭제 실패-존재하지 않는 like 리소스(404 Not found)")
    public void deleteLikeFail_Not_Found_Like() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);//post 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/likes/{likeId}", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 삭제 실패-Post 리소스의 자식 like 리소스가 아닌 경우(409 Conflict)")
    public void deleteLikeFail_Conflict() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post1 = createPost(account, 0, 1, 1);//post 리소스 추가
        Post post2 = createPost(account, 0, 1, 1);//post 리소스 추가
        Like like = createLike(post2, account);//post2 게시물에 좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/likes/{likeId}", post1.getId(), like.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    private Like createLike(Post post, Account account) {
        Like like = Like.builder()
                .post(post)
                .account(account)
                .build();

        return likeRepository.save(like);//좋아요 리소스 추가
    }
}