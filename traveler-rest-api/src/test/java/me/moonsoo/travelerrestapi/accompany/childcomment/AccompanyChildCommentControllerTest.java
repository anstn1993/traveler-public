package me.moonsoo.travelerrestapi.accompany.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanyBaseControllerTest;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccompanyChildCommentControllerTest extends AccompanyBaseControllerTest {

    @AfterEach
    public void tearDown() {
        accompanyChildCommentRepository.deleteAll();
        accompanyCommentRepository.deleteAll();
        accompanyRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가")
    public void createChildComment() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is a child comment0");//요청 본문 dto

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("accompanyComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-child-comment").exists())
                .andExpect(jsonPath("_links.delete-accompany-child-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("create-accompany-child-comment",
                        links(
                                linkWithRel("self").description("추가된 동행 게시물 대댓글 리소스 링크"),
                                linkWithRel("get-accompany-child-comments").description("동행 게시물의 대댓글 목록 조회 링크"),
                                linkWithRel("update-accompany-child-comment").description("동행 게시물의 대댓글 수정 링크"),
                                linkWithRel("delete-accompany-child-comment").description("동행 게시물의 대댓글 삭제 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("동행 게시물의 댓글 id")
                        ),
                        requestFields(
                                fieldWithPath("comment").description("대댓글")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("추가한 댓글의 리소스 url"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("대댓글 id"),
                                fieldWithPath("account.id").description("작성자 id"),
                                fieldWithPath("accompany.id").description("동행 게시물 id"),
                                fieldWithPath("accompanyComment.id").description("부모 댓글 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("작성 시간"),
                                fieldWithPath("_links.self.href").description("추가된 동행 게시물 대댓글 리소스 링크"),
                                fieldWithPath("_links.get-accompany-child-comments.href").description("동행 게시물의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.update-accompany-child-comment.href").description("동행 게시물의 대댓글 수정 링크"),
                                fieldWithPath("_links.delete-accompany-child-comment.href").description("동행 게시물의 대댓글 삭제 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가 실패-요청 본문을 전달하지 않은 경우(400 Bad request)")
    public void createChildCommentFail_Empty_Value() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글


        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가 실패-허용되지 않은 값을 넘기는 경우(400 Bad request)")
    public void createChildCommentFail_Wrong_Value() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childComment)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가 실패-존재하지 않는 동행 게시물 리소스(404 Not found)")
    public void createChildCommentFail_Not_Existing_Accompany() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is a child comment0");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", 404, accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가 실패-존재하지 않는 댓글 리소스(404 Not found)")
    public void createChildCommentFail_Not_Existing_Comment() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is a child comment0");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물에 대댓글 추가 실패-동행 게시물에 존재하지 않는 댓글(409 Conflict)")
    public void createChildCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {

        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 1);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is a child comment0");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany2.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 동행 게시물의 대댓글 목록 조회(한 페이지에 10개씩, 1페이지, id를 기준으로 오름차순)")
    public void getAccompanyChildComments_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        //accompanyComment에 대댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createChildComment(account, accompany, accompanyComment, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].accompanyComment.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-accompany-child-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
                .andDo(document("get-accompany-child-comments",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-accompany-child-comment").description("대댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
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
                                fieldWithPath("_embedded.accompanyChildCommentList[].id").description("대댓글 id"),
                                fieldWithPath("_embedded.accompanyChildCommentList[].account.id").description("대댓글 작성자의 id"),
                                fieldWithPath("_embedded.accompanyChildCommentList[].accompany.id").description("동행 게시물 id"),
                                fieldWithPath("_embedded.accompanyChildCommentList[].accompanyComment.id").description("부모 댓글 id"),
                                fieldWithPath("_embedded.accompanyChildCommentList[].comment").description("대댓글"),
                                fieldWithPath("_embedded.accompanyChildCommentList[].regDate").description("대댓글 추가 시간"),
                                fieldWithPath("_embedded.accompanyChildCommentList[]._links.self.href").description("대댓글 리소스 url"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크"),
                                fieldWithPath("_links.create-accompany-child-comment.href").description("대댓글 추가 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 동행 게시물의 대댓글 목록 조회(한 페이지에 10개씩, 1페이지, id를 기준으로 오름차순)")
    public void getAccompanyChildComments_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        //accompanyComment에 대댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createChildComment(account, accompany, accompanyComment, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), accompanyComment.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].account.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].accompany.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].accompanyComment.id").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].comment").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0].regDate").exists())
                .andExpect(jsonPath("_embedded.accompanyChildCommentList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-accompany-child-comment").doesNotExist())//미인증 상태에서는 대댓글 생성 링크를 제거
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 목록 조회 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void getAccompanyChildComments_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        //accompanyComment에 대댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createChildComment(account, accompany, accompanyComment, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", 404, accompanyComment.getId())
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
    @DisplayName("동행 게시물의 대댓글 목록 조회 실패-존재하지 않는 댓글(404 Not found)")
    public void getAccompanyChildComments_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        //accompanyComment에 대댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createChildComment(account, accompany, accompanyComment, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany.getId(), 404)
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
    @DisplayName("동행 게시물의 대댓글 목록 조회 실패-조회한 동행 게시물의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getAccompanyChildComments_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 1);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글
        //accompanyComment에 대댓글 100개 생성
        IntStream.range(0, 100).forEach(i -> {
            createChildComment(account, accompany1, accompanyComment, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments", accompany2.getId(), accompanyComment.getId())
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
    @DisplayName("인증 상태에서 자신의 동행 게시물 대댓글 하나 조회")
    public void getMyAccompanyChildComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("accompanyComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                .andExpect(jsonPath("_links.update-accompany-child-comment").exists())
                .andExpect(jsonPath("_links.delete-accompany-child-comment").exists())
                .andDo(document("get-accompany-child-comment",
                        links(
                                linkWithRel("self").description("조회한 대댓글의 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accompany-child-comments").description("대댓글의 목록을 조회할 수 있는 링크"),
                                linkWithRel("update-accompany-child-comment").description("조회한 대댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                linkWithRel("delete-accompany-child-comment").description("조회한 대댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("댓글 id"),
                                parameterWithName("childCommentId").description("대댓글 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("대댓글의 id"),
                                fieldWithPath("account.id").description("댓글 작성자 id"),
                                fieldWithPath("accompany.id").description("동행 게시물 id"),
                                fieldWithPath("accompanyComment.id").description("부모 댓글의 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("대댓글 추가 시간"),
                                fieldWithPath("_links.self.href").description("조회한 대댓글 리소스 url"),
                                fieldWithPath("_links.get-accompany-child-comments.href").description("대댓글 목록을 조회할 수 있는 url"),
                                fieldWithPath("_links.update-accompany-child-comment.href").description("조회한 대댓글을 수정할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-accompany-child-comment.href").description("조회한 대댓글을 삭제할 수 있는 링크(인증상태에서 자신의 댓글을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 다른 사용자의 동행 게시물 대댓글 하나 조회")
    public void getOthersAccompanyChildComment_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(otherAccount, accompany, accompanyComment, 0);//다른 사용자가 단 대댓글

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("accompanyComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                //다른 사용자의 대댓글 조회시에는 수정, 삭제 링크 x
                .andExpect(jsonPath("_links.update-accompnay-child-comment").doesNotExist())
                .andExpect(jsonPath("_links.delete-accompnay-child-comment").doesNotExist())
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 동행 게시물의 대댓글 하나 조회")
    public void getAccompanyChildComment_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("accompanyComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                //미인증상태에서 대댓글 조회시에는 수정, 삭제 링크 x
                .andExpect(jsonPath("_links.update-accompnay-child-comment").doesNotExist())
                .andExpect(jsonPath("_links.delete-accompnay-child-comment").doesNotExist())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 하나 조회 실패-존재하지 않는 동행 게시물(404 Not Found)")
    public void getAccompanyChildComment_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", 404, accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 하나 조회 실패-존재하지 않는 댓글(404 Not Found)")
    public void getAccompanyChildComment_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), 404, childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 하나 조회 실패-존재하지 않는 대댓글(404 Not Found)")
    public void getAccompanyChildComment_Not_Existing_Child_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany1에 달린 댓글
        createChildComment(account, accompany, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 하나 조회 실패-동행 게시물에 존재하지 않는 댓글(409 Conflict)")
    public void getAccompanyChildComment_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany1, accompanyComment, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany2.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("동행 게시물의 대댓글 하나 조회 실패-댓글의 자식 댓글이 아닌 경우(409 Conflict)")
    public void getAccompanyChildComment_Not_Existing_Child_Comment_In_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment1 = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyComment accompanyComment2 = createComment(account, accompany, 1);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment1, 0);//accomoanyComment1에 달린 대댓글

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment2.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정")
    public void updateAccompanyChildComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("accompany.id").exists())
                .andExpect(jsonPath("accompanyComment.id").exists())
                .andExpect(jsonPath("comment").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-accompany-child-comments").exists())
                .andExpect(jsonPath("_links.delete-accompany-child-comment").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-accompany-child-comment",
                        links(
                                linkWithRel("self").description("수정한 대댓글의 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accompany-child-comments").description("대댓글의 목록을 조회할 수 있는 링크"),
                                linkWithRel("delete-accompany-child-comment").description("수정한 대댓글을 삭제할 수 있는 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
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
                                fieldWithPath("accompany.id").description("작성자 id"),
                                fieldWithPath("accompanyComment.id").description("부모 댓글 id"),
                                fieldWithPath("comment").description("대댓글"),
                                fieldWithPath("regDate").description("작성 시간"),
                                fieldWithPath("_links.self.href").description("추가된 동행 게시물 대댓글 리소스 링크"),
                                fieldWithPath("_links.get-accompany-child-comments.href").description("동행 게시물의 대댓글 목록 조회 링크"),
                                fieldWithPath("_links.delete-accompany-child-comment.href").description("동행 게시물의 대댓글 삭제 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-요청 본문이 비어있는 경우(400 Bad request)")
    public void updateAccompanyChildCommentFail_Empty_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-요청 본문에 허용되지 않은 값이 포함되는 경우(400 Bad request)")
    public void updateAccompanyChildCommentFail_Wrong_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childComment)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-인증하지 않은 경우(401 Unauthorized)")
    public void updateAccompanyChildCommentFail_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글
        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-타인의 대댓글을 수정하려고 하는 경우(403 Forbidden)")
    public void updateAccompanyChildCommentFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        Account otherAccount = createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        AccompanyChildComment childComment = createChildComment(otherAccount, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void updateAccompanyChildCommentFail_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", 404, accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-존재하지 않는 댓글(404 Not found)")
    public void updateAccompanyChildCommentFail_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), 404, childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-존재하지 않는 대댓글(404 Not found)")
    public void updateAccompanyChildCommentFail_Not_Existing_Child_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-게시물에 존재하지 않는 댓글(409 Conflict)")
    public void updateAccompanyChildCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany1, accompanyComment, 0);//accomoanyComment에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany2.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 수정 실패-부모댓글에 존재하지 않는 대댓글(409 Conflict)")
    public void updateAccompanyChildCommentFail_Not_Existing_Child_Comment_In_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment1 = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyComment accompanyComment2 = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment1, 0);//accomoanyComment1에 달린 대댓글

        AccompanyChildCommentDto childCommentDto = createChildCommentDto("This is updated comment");

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment2.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(childCommentDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제")
    public void deleteAccompanyChildComment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment1에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-accompany-child-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("accompanyId").description("동행 게시물 id"),
                                parameterWithName("commentId").description("댓글 id"),
                                parameterWithName("childCommentId").description("대댓글 id")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-인증하지 않은 경우(401 Unauthorized)")
    public void deleteAccompanyChildCommentFail_Unauthorized() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment1에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-타인의 대댓글을 삭제하려고 하는 경우(403 Forbidden)")
    public void deleteAccompanyChildCommentFail_Forbidden() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        Account otherAccount = createAccount(email, password, 1);
        accountRepository.save(otherAccount);
        AccompanyChildComment childComment = createChildComment(otherAccount, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-존재하지 않는 동행 게시물(404 Not found)")
    public void deleteAccompanyChildCommentFail_Not_Existing_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", 404, accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-존재하지 않는 댓글(404 Not found)")
    public void deleteAccompanyChildCommentFail_Not_Existing_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), 404, childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-존재하지 않는 대댓글(404 Not found)")
    public void deleteAccompanyChildCommentFail_Not_Existing_Child_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany, 0);//accompany에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment, 0);//accomoanyComment에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment.getId(), 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-게시물에 존재하지 않는 댓글(409 Conflict)")
    public void deleteAccompanyChildCommentFail_Not_Existing_Comment_In_Accompany() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany1 = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        Accompany accompany2 = createAccompany(account, 1);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment = createComment(account, accompany1, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany1, accompanyComment, 0);//accomoanyComment에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany2.getId(), accompanyComment.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("동행 게시물 대댓글 삭제 실패-해당 댓글에 달리지 않은 대댓글(409 Conflict)")
    public void deleteAccompanyChildCommentFail_Not_Existing_Child_Comment_In_Comment() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Accompany accompany = createAccompany(account, 0);//댓글이 달릴 동행 게시물
        AccompanyComment accompanyComment1 = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyComment accompanyComment2 = createComment(account, accompany, 0);//accompany1에 달린 댓글
        AccompanyChildComment childComment = createChildComment(account, accompany, accompanyComment1, 0);//accomoanyComment1에 달린 대댓글


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/accompanies/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}", accompany.getId(), accompanyComment2.getId(), childComment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }


    public AccompanyChildCommentDto createChildCommentDto(String comment) {
        return AccompanyChildCommentDto.builder()
                .comment(comment)
                .build();
    }
}