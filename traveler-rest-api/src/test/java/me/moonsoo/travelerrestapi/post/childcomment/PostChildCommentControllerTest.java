package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostBaseControllerTest;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.time.ZonedDateTime;
import java.util.stream.IntStream;

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


class PostChildCommentControllerTest extends PostBaseControllerTest {

    @Autowired
    private PostChildCommentRepository postChildCommentRepository;

    @AfterEach
    void tearDown() {
        postChildCommentRepository.deleteAll();
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("post 게시물의 대댓글 생성")
    public void createPostChildComment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("postComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-child-comment").exists())
                .andExpect(jsonPath("_links.delete-post-child-comment").exists())
                .andDo(document("create-post-child-comment",
                        links(
                                linkWithRel("self").description("추가된 post 게시물 대댓글 리소스 링크"),
                                linkWithRel("get-post-child-comments").description("post 게시물의 대댓글 목록 조회 링크"),
                                linkWithRel("update-post-child-comment").description("post 게시물의 대댓글 수정 링크"),
                                linkWithRel("delete-post-child-comment").description("post 게시물의 대댓글 삭제 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("post 게시물의 댓글 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("대댓글")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("추가한 댓글의 리소스 링크"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("대댓글 id"),
                                fieldWithPath("account.id").description("작성자 id"),
                                fieldWithPath("account.nickname").description("작성자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("작성자 프로필 이미지 경로"),
                                fieldWithPath("post.id").description("post 게시물 id"),
                                fieldWithPath("postComment.id").description("부모 댓글 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("작성 시간"),
                                fieldWithPath("_links.self.href").description("추가된 post 게시물 대댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-child-comments.href").description("post 게시물의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.update-post-child-comment.href").description("post 게시물의 대댓글 수정 링크"),
                                fieldWithPath("_links.delete-post-child-comment.href").description("post 게시물의 대댓글 삭제 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-요청 본문이 없는 경우(400 Bad request)")
    public void createPostChildCommentFail_No_Request_Body() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-요청 본문에 허용되지 않은 값이 있는 경우(400 Bad request)")
    public void createPostChildCommentFail_Not_Allowed_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);

        PostChildComment postChildCommentDto = createNotAllowedPostCommentDto();

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }


    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-값이 유효하지 않은 경우(400 Bad request)")
    public void createPostChildCommentFail_Wrong_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDtoWithWrongValue();

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void createPostChildCommentFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-존재하지 않는 post게시물(404 Not found)")
    public void createPostChildCommentFail_Not_Found_Post() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", 404, postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-존재하지 않는 댓글(404 Not found)")
    public void createPostChildCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 생성 실패-post게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void createPostChildCommentFail_Conflict() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post1 게시물 생성
        Post post2 = createPost(account, 1, 0, 1);//post2 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/comments/{commentId}/child-comments", post2.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;

    }

    @Test
    @DisplayName("인증 상태에서 post 게시물의 대댓글 목록 조회")
    public void getPostChildComments_With_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        //대댓글 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPostChildComment(post, postComment, account, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postChildCommentList").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.nickname").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].post.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].postComment.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-post-child-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
                .andDo(document("get-post-child-comments",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-post-child-comment").description("대댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id, regDate-등록 날짜)")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responsePageFields.and(
                                fieldWithPath("_embedded.postChildCommentList[].id").description("대댓글 id"),
                                fieldWithPath("_embedded.postChildCommentList[].account.id").description("대댓글 작성자의 id"),
                                fieldWithPath("_embedded.postChildCommentList[].account.nickname").description("대댓글 작성자의 닉네임"),
                                fieldWithPath("_embedded.postChildCommentList[].account.profileImageUri").description("대댓글 작성자의 프로필 이미지 경로"),
                                fieldWithPath("_embedded.postChildCommentList[].post.id").description("post 게시물 id"),
                                fieldWithPath("_embedded.postChildCommentList[].postComment.id").description("부모 댓글 id"),
                                fieldWithPath("_embedded.postChildCommentList[].comment").description("대댓글"),
                                fieldWithPath("_embedded.postChildCommentList[].regDate").description("대댓글 추가 시간"),
                                fieldWithPath("_embedded.postChildCommentList[]._links.self.href").description("대댓글 리소스 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.create-post-child-comment.href").description("대댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 post 게시물의 대댓글 목록 조회")
    public void getPostChildComments_Without_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        //대댓글 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPostChildComment(post, postComment, account, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), postComment.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postChildCommentList").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.nickname").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].post.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].postComment.id").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.postChildCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-post-child-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 목록 조회 실패-존재하지 않는 post 게시물(404 Not found)")
    public void getPostChildCommentsFail_Not_Found_Post() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments", 404, postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 목록 조회 실패-존재하지 않는 comment 게시물(404 Not found)")
    public void getPostChildCommentsFail_Not_Found_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments", post.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물의 대댓글 목록 조회 실패-post게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getPostChildCommentsFail_Conflict() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post1 게시물 생성
        Post post2 = createPost(account, 1, 0, 1);//post2 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성

        //대댓글 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPostChildComment(post1, postComment, account, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments", post2.getId(), postComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 자신의 post 게시물 대댓글 하나 조회")
    public void getMyPostChildComment_With_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("postComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-child-comment").exists())
                .andExpect(jsonPath("_links.delete-post-child-comment").exists())
                .andDo(document("get-post-child-comment",
                        links(
                                linkWithRel("self").description("조회한 대댓글의 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-post-child-comments").description("대댓글의 목록을 조회할 수 있는 링크"),
                                linkWithRel("update-post-child-comment").description("조회한 대댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("delete-post-child-comment").description("조회한 대댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id"),
                                parameterWithName("childCommentId").description("대댓글 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("대댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("account.nickname").description("댓글 작성자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("댓글 작성자 프로필 이미지 경로"),
                                fieldWithPath("post.id").description("post 게시물 id"),
                                fieldWithPath("postComment.id").description("부모 댓글의 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("대댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("조회한 대댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-child-comments.href").description("대댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-post-child-comment.href").description("조회한 대댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-post-child-comment.href").description("조회한 대댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 타인의 post 게시물 대댓글 하나 조회")
    public void getOthersPostChildComment_With_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, otherAccount, 0);//다른 사용자의 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("postComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-child-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post-child-comment").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 post 게시물 대댓글 하나 조회")
    public void getPostChildComment_Without_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("postComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.update-post-child-comment").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post-child-comment").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 하나 조회 실패-존재하지 않는 post 게시물(404 Not found)")
    public void getPostChildCommentFail_Not_Found_Post() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", 404, postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 하나 조회 실패-존재하지 않는 댓글(404 Not found)")
    public void getPostChildCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), 404, postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 하나 조회 실패-존재하지 않는 대댓글(404 Not found)")
    public void getPostChildCommentFail_Not_Found_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 하나 조회 실패-post게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getPostChildCommentFail_Conflict_Post_And_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post 게시물 생성
        Post post2 = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post1, postComment, account, 0);//대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post2.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 하나 조회 실패-댓글의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getPostChildCommentFail_Conflict_Comment_And_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment1 = createPostComment(0, account, post);//post에 댓글 생성
        PostComment postComment2 = createPostComment(1, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment1, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment2.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정")
    public void updatePostChildComment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("post.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-child-comments").exists())
                .andExpect(jsonPath("_links.delete-post-child-comment").exists())
                .andDo(document("update-post-child-comment",
                        links(
                                linkWithRel("self").description("수정한 대댓글의 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-post-child-comments").description("대댓글의 목록을 조회할 수 있는 링크"),
                                linkWithRel("delete-post-child-comment").description("수정한 대댓글을 삭제할 수 있는 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id"),
                                parameterWithName("childCommentId").description("대댓글 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("대댓글")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("대댓글 id"),
                                fieldWithPath("account.id").description("작성자 id"),
                                fieldWithPath("account.nickname").description("작성자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("작성자 프로필 이미지"),
                                fieldWithPath("post.id").description("작성자 id"),
                                fieldWithPath("postComment.id").description("부모 댓글 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("작성 시간"),
                                fieldWithPath("_links.self.href").description("추가된 post 게시물 대댓글 리소스 링크"),
                                fieldWithPath("_links.get-post-child-comments.href").description("post 게시물의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.delete-post-child-comment.href").description("post 게시물의 대댓글 삭제 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-요청 본문이 없는 경우(400 Bad request)")
    public void updatePostChildCommentFail_No_Request_Body() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-요청 본문의 값이 유효하지 않은 경우(400 Bad request)")
    public void updatePostChildCommentFail_Wrong_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDtoWithWrongValue();//유효하지 않은 값이 들어간 dto

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-요청 본문에 허용되지 않은 값이 있는 경우(400 Bad request)")
    public void updatePostChildCommentFail_Not_Allowed_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildComment postChildCommentDto = createNotAllowedPostCommentDto();//허용되지 않은 값이 들어간 dto

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void updatePostChildCommentFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-다른 사용자의 댓글을 수정하려고 하는 경우(403 Forbidden)")
    public void updatePostChildCommentFail_Forbidden() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, otherAccount, 0);//다른 사용자의 대댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-존재하지 않는 post게시물(404 Not found)")
    public void updatePostChildCommentFail_Not_Found_Post() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", 404, postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-존재하지 않는 댓글(404 Not found)")
    public void updatePostChildCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), 404, postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-존재하지 않는 대댓글(404 Not found)")
    public void updatePostChildCommentFail_Not_Found_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-post게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void updatePostChildCommentFail_Conflict_Post_And_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post1 게시물 생성
        Post post2 = createPost(account, 1, 0, 1);//post2 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post1, postComment, account, 0);

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post2.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post 게시물 대댓글 수정 실패-댓글의 자식 대댓글이 아닌 경우(409 Conflict)")
    public void updatePostChildCommentFail_Conflict_Comment_And_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment1 = createPostComment(0, account, post);//post에 댓글 생성
        PostComment postComment2 = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment1, account, 0);//postComment1에 대댓글 생성

        PostChildCommentDto postChildCommentDto = createPostChildCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment2.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postChildCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제")
    public void deletePostChildComment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-post-child-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post 게시물 id"),
                                parameterWithName("commentId").description("댓글 id"),
                                parameterWithName("childCommentId").description("대댓글 id")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void deletePostChildCommentFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-다른 사용자의 리소스를 삭제하려고 하는 경우(403 Forbidden)")
    public void deletePostChildCommentFail_Forbidden() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, otherAccount, 0);//다른 사용자의 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-존재하지 않는 post 리소스(404 Not found)")
    public void deletePostChildCommentFail_Not_Found_Post() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", 404, postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-존재하지 않는 댓글 리소스(404 Not found)")
    public void deletePostChildCommentFail_Not_Found_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), 404, postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-존재하지 않는 대댓글 리소스(404 Not found)")
    public void deletePostChildCommentFail_Not_Found_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post);//post에 댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", 404, postComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-post 게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void deletePostChildCommentFail_Conflict_Post_And_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post 게시물 생성
        Post post2 = createPost(account, 1, 0, 1);//post 게시물 생성
        PostComment postComment = createPostComment(0, account, post1);//post1에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post1, postComment, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post2.getId(), postComment.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("post게시물 대댓글 삭제 실패-댓글의 자식 대댓글이 아닌 경우(409 Conflict)")
    public void deletePostChildCommentFail_Conflict_Comment_And_Child_Comment() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Post post = createPost(account, 0, 0, 1);//post 게시물 생성
        PostComment postComment1 = createPostComment(0, account, post);//post에 댓글 생성
        PostComment postComment2 = createPostComment(1, account, post);//post에 댓글 생성
        PostChildComment postChildComment = createPostChildComment(post, postComment1, account, 0);//postComment1에 대댓글 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}/child-comments/{childCommentId}", post.getId(), postComment2.getId(), postChildComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    private PostChildCommentDto createPostChildCommentDto(int index) {
        return PostChildCommentDto.builder()
                .comment("comment" + index)
                .build();
    }

    private PostChildCommentDto createPostChildCommentDtoWithWrongValue() {
        return PostChildCommentDto.builder()
                .comment(" ")
                .build();
    }

    //허용되지 않은 값이 포함된 요청 본문 생성
    private PostChildComment createNotAllowedPostCommentDto() {
        return PostChildComment.builder()
                .id(100)
                .comment("댓글")
                .regDate(ZonedDateTime.now())
                .build();
    }

    private PostChildComment createPostChildComment(Post post, PostComment postComment, Account account, int index) {
        PostChildComment postChildComment = PostChildComment.builder()
                .account(account)
                .post(post)
                .postComment(postComment)
                .comment("comment" + index)
                .regDate(ZonedDateTime.now())
                .build();
        return postChildCommentRepository.save(postChildComment);
    }

}
