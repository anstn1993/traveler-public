package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostCommentControllerTest extends PostBaseControllerTest {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @AfterEach
    void tearDown() {
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가")
    public void createPostComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.update-post-comment").exists())
                .andExpect(jsonPath("_links.delete-post-comment").exists())
                .andDo(document("create-post-comment",
                        links(
                                linkWithRel("self").description("추가된 post 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-post-comments").description("post 게시물 댓글 리스트를 조회할 수 있는 링크"),
                                linkWithRel("update-post-comment").description("추가된 댓글을 수정할 수 있는 링크"),
                                linkWithRel("delete-post-comment").description("추가된 댓글을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("postId").description("댓글을 추가할 post 게시물 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("댓글")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("추가한 댓글의 리소스 링크"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("추가한 댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("post.id").description("댓글이 추가된 post 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("추가된 댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-post-comment.href").description("추가된 댓글을 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-post-comment.href").description("추가된 댓글을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가 실패-요청 본문이 없는 경우(400 Bad request)")
    public void createPostCommentFail_Empty_Request_Body() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가 실패-요청 본문의 값이 유효하지 않은 경우(400 Bad request)")
    public void createPostCommentFail_WrongValue() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostCommentDto postCommentDto = createPostCommentDtoWithWrongValue();//비즈니스 로직에 안 맞는 요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가 실패-요청 본문에 허용되지 않은 값이 포함된 경우(400 Bad request)")
    public void createPostCommentFail_Not_Allowed_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostComment postCommentDto = createNotAllowedPostCommentDto();

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가 실패-oauth 인증을 하지 않은 경우(401 Unauthorized)")
    public void createPostCommentFail_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post게시물에 댓글 리소스 추가 실패-존재하지 않는 post게시물(404 Not found)")
    public void createPostCommentFail_Not_Found() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 post 게시물의 댓글 목록 조회")
    public void getPostComments_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        //댓글 리소스 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPostComment(i, account, post);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postCommentList").exists())
                .andExpect(jsonPath("_embedded.postCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.postCommentList[0]._links.get-post-child-comments").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-post-comment").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andDo(document("get-post-comments",
                        pagingLinks.and(
                                linkWithRel("self").description("현재 페이지 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-post-comment").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("댓글을 추가할 post 게시물 id")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id, regDate-등록 날짜)")
                        )
                        , responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        )
                        , responsePageFields.and(
                                fieldWithPath("_embedded.postCommentList[].id").description("댓글 id"),
                                fieldWithPath("_embedded.postCommentList[].account.id").description("댓글 작성자의 id"),
                                fieldWithPath("_embedded.postCommentList[].post.id").description("댓글이 달린 post 게시물 id"),
                                fieldWithPath("_embedded.postCommentList[].comment").description("댓글"),
                                fieldWithPath("_embedded.postCommentList[].regDate").description("댓글 추가 시간"),
                                fieldWithPath("_embedded.postCommentList[]._links.self.href").description("댓글 리소스 링크"),
                                fieldWithPath("_embedded.postCommentList[]._links.get-post-child-comments.href").description("댓글의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.create-post-comment.href").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }


    @Test
    @DisplayName("미인증 상태에서 post 게시물의 댓글 목록 조회")
    public void getPostComments_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        //댓글 리소스 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPostComment(i, account, post);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments", post.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postCommentList").exists())
                .andExpect(jsonPath("_embedded.postCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.postCommentList[0]._links.get-post-child-comments").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-post-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
        ;
    }

    @Test
    @DisplayName("post 게시물의 댓글 목록 조회 실패-존재하지 않는 post 게시물(404 Not found)")
    public void getPostCommentsFail_Not_Found() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 post 게시물의 자신의 댓글 하나 조회")
    public void getMyPostComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        Post post = createPost(account, 0, 1, 1);
        PostComment postComment = createPostComment(0, account, post);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-comment").exists())
                .andExpect(jsonPath("_links.delete-post-comment").exists())
                .andDo(document("get-post-comment",
                        links(
                                linkWithRel("self").description("조회한 post 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-post-child-comments").description("조회한 post 게시물 댓글의 대댓글 목록 조회 링크"),
                                linkWithRel("get-post-comments").description("post 게시물 댓글 목록을 조회할 수 있는 링크"),
                                linkWithRel("update-post-comment").description("조회한 댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("delete-post-comment").description("조회한 댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("post.id").description("post 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("조회한 댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-child-comments.href").description("조회한 post 게시물 댓글의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.get-post-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-post-comment.href").description("조회한 댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-post-comment.href").description("조회한 댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 post 게시물의 다른 사용자의 댓글 하나 조회")
    public void getOthersPostComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        Post post = createPost(account, 0, 1, 1);
        PostComment postComment = createPostComment(0, otherAccount, post);//다른 사용자가 생성한 댓글 리소스

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post-comment").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 post 게시물의 댓글 하나 조회")
    public void getPostComment_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 1);
        Post post = createPost(account, 0, 1, 1);
        PostComment postComment = createPostComment(0, account, post);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post-comment").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("post 게시물의 댓글 하나 조회 실패-존재하지 않는 post게시물(404 Not found)")
    public void getPostCommentFail_Not_Found_Post() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        Post post = createPost(account, 0, 1, 1);
        PostComment postComment = createPostComment(0, account, post);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", 404, postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 댓글 하나 조회 실패-존재하지 않는 댓글(404 Not found)")
    public void getPostCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        Post post = createPost(account, 0, 1, 1);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 댓글 하나 조회 실패-Post게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getPostCommentFail_Conflict() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        Post post1 = createPost(account, 0, 1, 1);
        Post post2 = createPost(account, 1, 1, 1);
        PostComment postComment = createPostComment(0, account, post1);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", post2.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정")
    public void updatePostComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.delete-post-comment").exists())
                .andDo(document("update-post-comment",
                        links(
                                linkWithRel("self").description("수정된 post 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-post-comments").description("post 게시물 댓글 목록을 조회할 수 있는 링크"),
                                linkWithRel("delete-post-comment").description("댓글을 삭제할 수 있는 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("댓글")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("post.id").description("post 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-post-comment.href").description("댓글을 삭제할 수 있는 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-요청 본문이 없는 경우(400 Bad request)")
    public void updatePostCommentFail_Empty_Request_Body() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-요청 본문의 값이 유효하지 않은 경우(400 Bad request)")
    public void updatePostCommentFail_Wrong_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        PostCommentDto postCommentDto = createPostCommentDtoWithWrongValue();//값이 유효하지 않은 요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-요청 본문에 허용되지 않은 값이 포함된 경우(400 Bad request)")
    public void updatePostCommentFail_Not_Allowed_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        PostComment postCommentDto = createNotAllowedPostCommentDto();//허용되지 않은 값이 포함된 요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-인증을 하지 않은 경우(401 Unauthorized)")
    public void updatePostCommentFail_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-다른 사용자의 댓글을 수정하려고 하는 경우(403 Forbidden)")
    public void updatePostCommentFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);//다른 사용자 생성
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, otherAccount, post);//다른 사용자의 댓글 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-post리소스가 존재하지 않는 경우(404 Not found)")
    public void updatePostCommentFail_Not_Found_Post() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//댓글 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", 404, postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-댓글 리소스가 존재하지 않는 경우(404 Not found)")
    public void updatePostCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 댓글 수정 실패-댓글이 post게시물의 자식이 아닌 경우(409 Conflict)")
    public void updatePostCommentFail_Conflict() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post 게시물 생성
        Post post2 = createPost(account, 1, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성

        PostCommentDto postCommentDto = createPostCommentDto(0);//요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", post2.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }


    private PostCommentDto createPostCommentDto(int index) {
        return PostCommentDto.builder()
                .comment("comment" + index)
                .build();
    }


    private PostCommentDto createPostCommentDtoWithWrongValue() {
        return PostCommentDto.builder()
                .comment("  ")
                .build();
    }

    private PostComment createNotAllowedPostCommentDto() {
        return PostComment.builder()
                .comment("comment")
                .id(100)
                .regDate(LocalDateTime.now())
                .build();
    }

    private PostComment createPostComment(int index, Account account, Post post) {
        PostComment postComment = PostComment.builder()
                .post(post)
                .account(account)
                .comment("comment" + index)
                .regDate(LocalDateTime.now())
                .build();

        return postCommentRepository.save(postComment);
    }
}