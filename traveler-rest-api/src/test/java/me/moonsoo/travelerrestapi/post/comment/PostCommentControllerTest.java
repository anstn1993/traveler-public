package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.time.LocalDateTime;

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
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
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
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(postCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
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
}