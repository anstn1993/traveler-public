package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanyBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest extends AccompanyBaseControllerTest {

    @Autowired
    CommentRepository commentRepository;

    @AfterEach
    public void setUp() {
        commentRepository.deleteAll();
        accompanyRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가")
    public void createComment() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물

        CommentDto commentDto = createCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-comment").exists())
                .andExpect(jsonPath("_links.delete-accompany-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                //rest docs 적용
                .andDo(document("create-accompany-comment",
                        links(
                                linkWithRel("self").description("추가된 동행 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-accompany-comments").description("동행 게시물 댓글 리스트를 조회할 수 있는 url"),
                                linkWithRel("update-accompany-comment").description("추가된 댓글을 수정할 수 있는 url"),
                                linkWithRel("delete-accompany-comment").description("추가된 댓글을 삭제할 수 있는 url"),
                                linkWithRel("profile").description("api 문서 url")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("댓글을 추가할 동행 게시물 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("댓글")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("추가한 댓글의 리소스 url"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("추가한 댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("accompany.id").description("댓글이 추가된 동행 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("추가된 댓글 리소스 url"),
                                fieldWithPath("_links.get-accompany-comments.href").description("댓글 목록을 조회할 수 있는 url"),
                                fieldWithPath("_links.update-accompany-comment.href").description("추가된 댓글을 수정할 수 있는 url"),
                                fieldWithPath("_links.delete-accompany-comment.href").description("추가된 댓글을 삭제할 수 있는 url"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가 실패-요청 본문의 값이 없는 경우(400 Bad Request)")
    public void createCommentFail_Empty_Value() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password);
        Accompany accompany = createAccompany(account, 0);


        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가 실패-요청 본문에 허용되지 않은 값이 들어있는 경우(400 Bad Request)")
    public void createCommentFail_Unknown_Value() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password);
        Accompany accompany = createAccompany(account, 0);

        Comment comment = Comment.builder()
                .id(100)//허용되지 않은 값
                .account(account)//허용되지 않은 값
                .accompany(accompany)//허용되지 않은 값
                .regDate(LocalDateTime.now())//허용되지 않은 값
                .comment("This is comment.")
                .build();


        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(comment)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가 실패-존재하지 않는 동행 게시물에 댓글을 다려고 하는 경우(400 Bad Request)")
    public void createCommentFail_Not_Existing_Accompany() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password);
        CommentDto commentDto = createCommentDto(0);

        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", 404)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가 실패-인증하지 않은 경우(401 Unauthorized)")
    public void createCommentFail_Unauthorized() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password);
        Accompany accompany = createAccompany(account, 0);

        CommentDto commentDto = createCommentDto(0);


        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("미인증 상태에서 동행 게시판의 댓글 목록 조회-100개의 게시물, 한 페이지에 10개씩, 두 번째 페이지 오름차순 조회")
    public void getAccompanyComments_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password);//계정 생성
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        //댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createComment(accompany, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.commentList[0].id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.commentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.commentList[0]._links.self.href").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
        ;

    }

    @Test
    @DisplayName("인증 상태에서 동행 게시판의 댓글 목록 조회-100개의 게시물, 한 페이지에 10개씩, 두 번째 페이지 오름차순 조회")
    public void getAccompanyComments_WithAuth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        //댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createComment(accompany, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.commentList[0].id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.commentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.commentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.commentList[0]._links.self.href").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-accompany-comment").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andDo(document("get-accompany-comments",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-accompany-comment").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("댓글을 추가할 동행 게시물 id")
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
                                fieldWithPath("_embedded.commentList[0].id").description("댓글 id"),
                                fieldWithPath("_embedded.commentList[0].account.id").description("댓글 작성자의 id"),
                                fieldWithPath("_embedded.commentList[0].accompany.id").description("댓글이 달린 동행 게시물 id"),
                                fieldWithPath("_embedded.commentList[0].comment").description("댓글"),
                                fieldWithPath("_embedded.commentList[0].regDate").description("댓글 추가 시간"),
                                fieldWithPath("_embedded.commentList[0]._links.self.href").description("댓글 리소스 url"),
                                fieldWithPath("_links.create-accompany-comment.href").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("존재하지 않는 동행 게시물의 댓글을 조회(400 Bad request)")
    public void getAccompanyComments_NotExist_Accompany() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", 404)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }


    private Comment createComment(Accompany accompany, int index) {
        Comment comment = Comment.builder()
                .comment("This is comment" + index)
                .account(account)
                .accompany(accompany)
                .regDate(LocalDateTime.now())
                .build();
        return commentRepository.save(comment);
    }

    private CommentDto createCommentDto(int index) {
        return CommentDto.builder()
                .comment("This is comment" + index)
                .build();
    }
}