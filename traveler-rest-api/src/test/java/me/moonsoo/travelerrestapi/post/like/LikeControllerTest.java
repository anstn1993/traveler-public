package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest extends PostBaseControllerTest {

    @Autowired
    private LikeRepository likeRepository;

    @AfterEach
    public void setUp() {
        postImageRepository.deleteAll();
        postTagRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("좋아요 리소스 추가")
    public void createLike() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("account.id").exists())
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
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
        String email = "user@email.com";
        String password = "user";
        account = createAccount(email, password, 0);
        Post post = createPost(account, 0, 1, 1);

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 추가 실패-존재하지 않는 post 리소스인 경우(404 Not found)")
    public void createLikeFail_Not_Found() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        mockMvc.perform(post("/api/posts/{postId}/likes", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("좋아요 리소스 추가 실패-이미 좋아요를 한 게시물인 경우(409 Conflict)")
    public void createLikeFail_Conflict() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Post post = createPost(account, 0, 1, 1);

        Like like = Like.builder()
                .post(post)
                .account(account)
                .build();

        likeRepository.save(like);//좋아요 리소스 추가

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/likes", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}