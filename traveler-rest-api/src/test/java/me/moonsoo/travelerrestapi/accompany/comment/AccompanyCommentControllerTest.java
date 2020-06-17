package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanyBaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccompanyCommentControllerTest extends AccompanyBaseControllerTest {

    @AfterEach
    public void setUp() {
        accompanyChildCommentRepository.deleteAll();
        accompanyCommentRepository.deleteAll();
        accompanyRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가")
    public void createComment() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물

        AccompanyCommentDto accompanyCommentDto = createCommentDto(0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
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
                                linkWithRel("get-accompany-comments").description("동행 게시물 댓글 리스트를 조회할 수 있는 링크"),
                                linkWithRel("update-accompany-comment").description("추가된 댓글을 수정할 수 있는 링크"),
                                linkWithRel("delete-accompany-comment").description("추가된 댓글을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("댓글을 추가할 동행 게시물 id")
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
                                fieldWithPath("accompany.id").description("댓글이 추가된 동행 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("추가된 댓글 리소스 링크"),
                                fieldWithPath("_links.get-accompany-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-accompany-comment.href").description("추가된 댓글을 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-accompany-comment.href").description("추가된 댓글을 삭제할 수 있는 링크"),
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
        String accessToken = getAuthToken(email, password, 0);
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
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);

        AccompanyComment accompanyComment = AccompanyComment.builder()
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
                .content(objectMapper.writeValueAsString(accompanyComment)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동행 게시판 댓글 추가 실패-존재하지 않는 동행 게시물에 댓글을 다려고 하는 경우(400 Bad Request)")
    public void createCommentFail_Not_Existing_Accompany() throws Exception {

        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        AccompanyCommentDto accompanyCommentDto = createCommentDto(0);

        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", 404)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
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
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);

        AccompanyCommentDto accompanyCommentDto = createCommentDto(0);


        mockMvc.perform(post("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("미인증 상태에서 동행 게시판의 댓글 목록 조회-100개의 게시물, 한 페이지에 10개씩, 두 번째 페이지 오름차순 조회")
    public void getAccompanyComments_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);//계정 생성
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        //댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createComment(account, accompany, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0]._links.self.href").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0]._links.get-accompany-child-comments.href").exists())
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
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        //댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createComment(account, accompany, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", accompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0]._links.self.href").exists())
                .andExpect(jsonPath("_embedded.accompanyCommentList[0]._links.get-accompany-child-comments.href").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-accompany-comment").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andDo(document("get-accompany-comments",
                        pagingLinks.and(
                                linkWithRel("self").description("현재 페이지 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-accompany-comment").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
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
                                fieldWithPath("_embedded.accompanyCommentList[].id").description("댓글 id"),
                                fieldWithPath("_embedded.accompanyCommentList[].account.id").description("댓글 작성자의 id"),
                                fieldWithPath("_embedded.accompanyCommentList[].accompany.id").description("댓글이 달린 동행 게시물 id"),
                                fieldWithPath("_embedded.accompanyCommentList[].comment").description("댓글"),
                                fieldWithPath("_embedded.accompanyCommentList[].regDate").description("댓글 추가 시간"),
                                fieldWithPath("_embedded.accompanyCommentList[]._links.self.href").description("댓글 리소스 링크"),
                                fieldWithPath("_embedded.accompanyCommentList[]._links.get-accompany-child-comments.href").description("댓글의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.create-accompany-comment.href").description("댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("존재하지 않는 동행 게시물의 댓글을 조회(404 not found)")
    public void getAccompanyComments_NotExist_Accompany() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments", 404)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))//sort cirteria: id, regDate
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 자신의 동행 게시물 댓글 하나 조회")
    public void getMyAccompanyComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                .andExpect(jsonPath("_links.get-accompany-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-comment").exists())
                .andExpect(jsonPath("_links.delete-accompany-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-accompany-comment",
                        links(
                                linkWithRel("self").description("조회한 동행 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-accompany-child-comments").description("조회한 동행 게시물 댓글의 대댓글 목록 조회 링크"),
                                linkWithRel("get-accompany-comments").description("동행 게시물 댓글 목록을 조회할 수 있는 링크"),
                                linkWithRel("update-accompany-comment").description("조회한 댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("delete-accompany-comment").description("조회한 댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("accompany.id").description("동행 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("조회한 댓글 리소스 링크"),
                                fieldWithPath("_links.get-accompany-child-comments.href").description("조회한 동행 게시물 댓글의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.get-accompany-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-accompany-comment.href").description("조회한 댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-accompany-comment.href").description("조회한 댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 다른 사용자의 동행 게시물 댓글 하나 조회")
    public void getOthersAccompanyComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(otherAccount, accompany, 0);//다른 사용자가 단 댓글

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-comment").doesNotExist())
                .andExpect(jsonPath("_links.delete-accompany-comment").doesNotExist())
                .andExpect(jsonPath("_links.profile").exists())

        ;
    }

    @Test
    @DisplayName("미인증 상태에서 동행 게시물 댓글 하나 조회")
    public void getAccompanyComment_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-comment").doesNotExist())
                .andExpect(jsonPath("_links.delete-accompany-comment").doesNotExist())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 하나 조회 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void getAccompanyCommentFail_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(get("/api/accompanies/404/comments/{commentId}", accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 하나 조회 실패-존재하지 않는 댓글(404 Not found)")
    public void getAccompanyCommentFail_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물

        mockMvc.perform(get("/api/accompanies/{accompanyId}/comments/404", accompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 하나 조회 실패-게시물에 존재하지 않는 댓글(404 Not found)")
    public void getAccompanyCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 1);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글

        //accompany2에서 accompany1에 달린 댓글을 조회
        mockMvc.perform(get("/api/accompanies/{accompanyId}/comments/{commentId}", accompany2.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정")
    public void updateAccompanyComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        AccompanyCommentDto accompanyCommentDto = modelMapper.map(accompanyComment, AccompanyCommentDto.class);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-comments").exists())
                .andExpect(jsonPath("_links.delete-accompany-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-accompany-comment",
                        links(
                                linkWithRel("self").description("동행 게시물 댓글의 리소스 링크"),
                                linkWithRel("get-accompany-comments").description("동행 게시물 댓글 리스트를 조회할 수 있는 링크"),
                                linkWithRel("delete-accompany-comment").description("댓글을 삭제할 수 있는 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("댓글")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("accompany.id").description("동행 게시물의 id"),
                                fieldWithPath("comment").description("댓글"),
                                fieldWithPath("regDate").description("댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("댓글 리소스 링크"),
                                fieldWithPath("_links.get-accompany-comments.href").description("댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-accompany-comment.href").description("댓글을 삭제할 수 있는 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-요청 본문이 없는 경우(400 Bad request)")
    public void updateAccompanyCommentFail_Empty_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-허용되지 않은 값이 넘어온 경우(400 Bad request)")
    public void updateAccompanyCommentFail_Wrong_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyComment)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-요청 본문이 없는 경우(401 Unauthorized)")
    public void updateAccompanyCommentFail_Unauthroized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-다른 사용자의 댓글을 수정하려고 하는 경우(403 Forbidden)")
    public void updateAccompanyCommentFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount =createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(otherAccount, accompany, 0);//다른 사용자가 작성한 댓글
        AccompanyCommentDto accompanyCommentDto = modelMapper.map(accompanyComment, AccompanyCommentDto.class);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void updateAccompanyCommentFail_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        AccompanyCommentDto accompanyCommentDto = modelMapper.map(accompanyComment, AccompanyCommentDto.class);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/404/comments/{commentId}", accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;

    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-존재하지 않는 댓글(404 Not found)")
    public void updateAccompanyCommentFail_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        AccompanyCommentDto accompanyCommentDto = modelMapper.map(accompanyComment, AccompanyCommentDto.class);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/404", accompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 수정 실패-게시물에 존재하지 않는 댓글(404 Not found)")
    public void updateAccompanyCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2= createAccompany(account, 1);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글

        AccompanyCommentDto accompanyCommentDto = modelMapper.map(accompanyComment, AccompanyCommentDto.class);

        //accompany2에서 accompany1에 달린 댓글 조회
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}", accompany2.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accompanyCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;

    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제")
    public void deleteAccompanyComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-accompany-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("댓글 id")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("대댓글이 있는 동행 게시물 댓글 삭제")
    public void deleteAccompanyComment_With_Child_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);
        createChildComment(account, accompany, accompanyComment, 0);//대댓글 생성
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제 실패-인증하지 않은 경우(401 Unauthorized)")
    public void deleteAccompanyCommentFail_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);
        mockMvc.perform(delete("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제 실패-타인의 댓글을 삭제하는 경우(403 Forbidden)")
    public void deleteAccompanyCommentFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(otherAccount, accompany, 0);//다른 사용자가 만든 댓글

        mockMvc.perform(delete("/api/accompanies/{accompanyId}/comments/{commentId}", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void deleteAccompanyCommentFail_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(delete("/api/accompanies/404/comments/{commentId}", accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제 실패-존재하지 않는 댓글(404 Not found)")
    public void deleteAccompanyCommentFail_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);

        mockMvc.perform(delete("/api/accompanies/{accompanyId}/comments/404", accompany.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 댓글 삭제 실패-게시물에 존재하지 않는 댓글(404 Not found)")
    public void deleteAccompanyCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);

        //accompany2에서 accompany1에서 달린 댓글 조회
        mockMvc.perform(delete("/api/accompanies/{accompanyId}/comments/{commentId}", accompany2.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }



    private AccompanyCommentDto createCommentDto(int index) {
        return AccompanyCommentDto.builder()
                .comment("This is comment" + index)
                .build();
    }
}