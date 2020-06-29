package me.moonsoo.travelerrestapi.post.childcomment;

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

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
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
    public void createPostChildComment_No_Request_Body() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
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
    @DisplayName("post 게시물의 대댓글 생성 실패-값이 유효하지 않은 경우(400 Bad request)")
    public void createPostChildComment_Wrong_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
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
    public void createPostChildComment_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
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
    public void createPostChildComment_Not_Found_Post() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
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
    public void createPostChildComment_Not_Found_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
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
    public void createPostChildComment_Conflict() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Post post1 = createPost(account, 0, 0, 1);//post1 게시물 생성
        Post post2 = createPost(account, 0, 0, 1);//post2 게시물 생성
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

}
