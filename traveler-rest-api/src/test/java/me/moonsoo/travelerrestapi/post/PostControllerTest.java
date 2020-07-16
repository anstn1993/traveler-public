package me.moonsoo.travelerrestapi.post;

import io.findify.s3mock.S3Mock;
import me.moonsoo.commonmodule.account.Account;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



class PostControllerTest extends PostBaseControllerTest {

    @BeforeAll
    public static void startMockS3Server(@Autowired S3Mock s3Mock) {
        s3Mock.stop();
        s3Mock.start();
    }

    @AfterAll
    public static void closeMockS3Server(@Autowired S3Mock s3Mock) {
        s3Mock.stop();
    }


    @AfterEach
    public void tearDown() {
        postImageRepository.deleteAll();
        postTagRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("post 게시물 추가 테스트")
    public void createPost() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //이미지 파일 part
        MockMultipartFile mockFile1 = createMockMultipartFile();

        //post part
        PostDto postDto = createPostDto(0, 5);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(multipart("/api/posts")
                .part(part)
                .file(mockFile1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("postTagList[0].id").exists())
                .andExpect(jsonPath("postTagList[0].post.id").exists())
                .andExpect(jsonPath("postTagList[0].tag").exists())
                .andExpect(jsonPath("postImageList[0].id").exists())
                .andExpect(jsonPath("postImageList[0].post.id").exists())
                .andExpect(jsonPath("postImageList[0].uri").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-posts").exists())
                .andExpect(jsonPath("_links.update-post").exists())
                .andExpect(jsonPath("_links.delete-post").exists())
                .andDo(document("create-post",
                        links(
                                linkWithRel("self").description("업로드된 post 게시물 리소스 링크"),
                                linkWithRel("get-posts").description("post 게시물 리스트를 조회할 수 있는 링크"),
                                linkWithRel("update-post").description("업로드된 post 게시물을 수정할 수 있는 링크"),
                                linkWithRel("delete-post").description("업로드된 post 게시물을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestParts(
                                partWithName("imageFiles").description("업로드할 이미지 파일 리스트"),
                                partWithName("post").description("post게시물의 dto json")
                        ),
                        requestPartFields(
                                "post",
                                fieldWithPath("article").description("게시물 본문"),
                                fieldWithPath("location").description("등록된 장소"),
                                fieldWithPath("latitude").description("장소의 위도"),
                                fieldWithPath("longitude").description("장소의 경도"),
                                fieldWithPath("tagList[].tag").description("게시물에 달 태그")
                        ),
                        requestHeaders,
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("업로드된 post 게시물 리소스 링크")
                        ),
                        responseFields(
                                fieldWithPath("id").description("post 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자 id"),
                                fieldWithPath("account.nickname").description("게시물 작성자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자 프로필 이미지 경로"),
                                fieldWithPath("article").description("게시물의 본문"),
                                fieldWithPath("postTagList[].id").description("게시물에 붙은 태그의 id"),
                                fieldWithPath("postTagList[].post.id").description("태그가 붙은 게시물의 id"),
                                fieldWithPath("postTagList[].tag").description("태그"),
                                fieldWithPath("postImageList[].id").description("게시물의 이미지 id"),
                                fieldWithPath("postImageList[].post.id").description("게시물의 id"),
                                fieldWithPath("postImageList[].uri").description("이미지의 uri"),
                                fieldWithPath("location").description("게시물에 등록한 장소명"),
                                fieldWithPath("latitude").description("장소의 위도"),
                                fieldWithPath("longitude").description("장소의 경도"),
                                fieldWithPath("regDate").description("게시물 작성 시간"),
                                fieldWithPath("viewCount").description("조회수"),
                                fieldWithPath("_links.self.href").description("업로드된 post 게시물 리소스 링크"),
                                fieldWithPath("_links.get-posts.href").description("post 게시물 리스트를 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-post.href").description("업로드된 post 게시물을 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-post.href").description("업로드된 post 게시물을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))  ;
    }


    @Test
    @DisplayName("post 게시물 추가 실패-post part가 없는 경우(400 Bad request)")
    public void createPostFail_Empty_Post_Part() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);


        MockMultipartFile mockFile1 = createMockMultipartFile();

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-post part에 허용되지 않은 값이 담기는 경우(400 bad request)")
    public void createPostFail_Unknown_property() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //이미지 파일 part
        MockMultipartFile mockFile1 = createMockMultipartFile();

        //post part
        Post postDto = createPostWithUnknowValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile1)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-post part에 비즈니스 로직에 맞지 않은 값이 담기는 경우(400 bad request)")
    public void createPostFail_Wrong_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 하나도 안 넘어온 경우(400 bad request)")
    public void createPostFail_No_Image_File() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 10개를 초과하는 경우(400 Bad request)")
    public void createPostFail_Exceed_Max_Image_Count() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 아닌 경우 415(Unsupported Media Type)")
    public void createPostFail_Not_Image_File() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFiles", "test.txt", "text/plain", "This is not a image file.".getBytes());

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-oauth 인증을 하지 않은 경우(401 Unauthorized)")
    public void createPostFail_Unauthorized() throws Exception {
        //파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFiles", "test.txt", "text/plain", "This is not a image file.".getBytes());

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 post게시물 목록 조회-30개의 게시물, 한 페이지에 10개씩 조회하고 2페이지 조회")
    public void getPosts_WithAuth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);
        //게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPost(account, i, 2, 2);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "tag")
                .param("search", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postList").exists())
                .andExpect(jsonPath("_embedded.postList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.postList[0]._links.get-post-comments").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-post").exists())
                .andDo(document("get-posts",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-post").description("post 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id, regDate-등록 날짜, viewCount-조회수)"),
                                parameterWithName("filter").optional().description("검색어 필터(writer-작성자, article-본문, location-장소명, tag-태그)"),
                                parameterWithName("search").optional().description("검색어")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responsePageFields.and(
                                fieldWithPath("_embedded.postList[].id").description("post 게시물의 id"),
                                fieldWithPath("_embedded.postList[].account.id").description("post 게시물 작성자의 id"),
                                fieldWithPath("_embedded.postList[].account.nickname").description("post 게시물 작성자의 닉네임"),
                                fieldWithPath("_embedded.postList[].account.profileImageUri").description("post 게시물 작성자의 프로필 이미지 경로"),
                                fieldWithPath("_embedded.postList[].article").description("post 게시물의 본문"),
                                fieldWithPath("_embedded.postList[].postTagList[].id").description("post 게시물의 tag id"),
                                fieldWithPath("_embedded.postList[].postTagList[].post.id").description("해당 tag가 달린 post게시물 id"),
                                fieldWithPath("_embedded.postList[].postTagList[].tag").description("tag"),
                                fieldWithPath("_embedded.postList[].postImageList[].id").description("post 게시물의 이미지 id"),
                                fieldWithPath("_embedded.postList[].postImageList[].post.id").description("해당 이미지가 게시된 post id"),
                                fieldWithPath("_embedded.postList[].postImageList[].uri").description("이미지 uri"),
                                fieldWithPath("_embedded.postList[].location").description("post 게시물에 달린 장소명"),
                                fieldWithPath("_embedded.postList[].latitude").description("장소의 위도"),
                                fieldWithPath("_embedded.postList[].longitude").description("장소의 경도"),
                                fieldWithPath("_embedded.postList[].regDate").description("post 게시물 작성 시간"),
                                fieldWithPath("_embedded.postList[].viewCount").description("post 게시물 조회수"),
                                fieldWithPath("_embedded.postList[]._links.self.href").description("post 게시물 리소스 조회 링크"),
                                fieldWithPath("_embedded.postList[]._links.get-post-comments.href").description("post 게시물의 댓글 목록 조회 링크"),
                                fieldWithPath("_links.create-post.href").description("post 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증하지 않은 상태에서 post게시물 목록 조회-30개의 게시물, 한 페이지에 10개씩 조회하고 2페이지 조회")
    public void getPosts_WithoutAuth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);
        //게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createPost(account, i, 2, 2);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "location")
                .param("search", "somewhere"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.postList").exists())
                .andExpect(jsonPath("_embedded.postList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.postList[0]._links.get-post-comments").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-post").doesNotExist())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 자신의 post 게시물 하나 조회")
    public void getMyPost_WithAuth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        Post post = createPost(account, 0, 2, 2);//자신의 게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("postImageList[0].id").exists())
                .andExpect(jsonPath("postImageList[0].post.id").exists())
                .andExpect(jsonPath("postImageList[0].uri").exists())
                .andExpect(jsonPath("postTagList[0].id").exists())
                .andExpect(jsonPath("postTagList[0].post.id").exists())
                .andExpect(jsonPath("postTagList[0].tag").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").value(post.getViewCount() + 1))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-posts").exists())
                .andExpect(jsonPath("_links.update-post").exists())
                .andExpect(jsonPath("_links.delete-post").exists())
                .andDo(document("get-post",
                        links(
                                linkWithRel("self").description("조회한 post 게시물 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-post-comments").description("조회한 post 게시물의 댓글 목록을 조회할 수 있는 링크"),
                                linkWithRel("get-posts").description("post 게시물 목록을 조회할 수 있는 링크"),
                                linkWithRel("update-post").description("post 게시물을 수정할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                linkWithRel("delete-post").description("post 게시물을 삭제할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("post 게시물의 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("post 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자의 id"),
                                fieldWithPath("account.nickname").description("게시물 작성자의 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자의 프로필 이미지 경로"),
                                fieldWithPath("article").description("post 게시물의 본문"),
                                fieldWithPath("postImageList[].id").description("post 게시물에 등록된 이미지 id"),
                                fieldWithPath("postImageList[].post.id").description("이미지가 등록된 게시물 id"),
                                fieldWithPath("postImageList[].uri").description("post 게시물에 등록된 이미지 uri"),
                                fieldWithPath("postTagList[].id").description("post 게시물에 달린 태그 id"),
                                fieldWithPath("postTagList[].post.id").description("tag가 달린 게시물 id"),
                                fieldWithPath("postTagList[].tag").description("tag"),
                                fieldWithPath("location").description("post 게시물에 등록한 장소의 경도"),
                                fieldWithPath("latitude").description("장소의 위도"),
                                fieldWithPath("longitude").description("장소의 경도"),
                                fieldWithPath("regDate").description("post 게시물 작성 시간"),
                                fieldWithPath("viewCount").description("post 게시물 조회수"),
                                fieldWithPath("_links.self.href").description("조회한 post 게시물의 리소스 링크"),
                                fieldWithPath("_links.get-post-comments.href").description("조회한 post 게시물의 댓글 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.get-posts.href").description("post 게시물 리스트를 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-post.href").description("post 게시물을 수정할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-post.href").description("post 게시물을 삭제할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 타인의 post 게시물 하나 조회")
    public void getOthersPost_WithAuth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(otherAccount, 0, 2, 2);//타인의 게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("postImageList[0].id").exists())
                .andExpect(jsonPath("postImageList[0].post.id").exists())
                .andExpect(jsonPath("postImageList[0].uri").exists())
                .andExpect(jsonPath("postTagList[0].id").exists())
                .andExpect(jsonPath("postTagList[0].post.id").exists())
                .andExpect(jsonPath("postTagList[0].tag").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").value(post.getViewCount() + 1))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-posts").exists())
                //타인의 게시물 조회시에는 수정, 삭제 링크 제공 x
                .andExpect(jsonPath("_links.update-post").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 post 게시물 하나 조회")
    public void getPost_WithoutAuth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 1);
        Post post = createPost(account, 0, 2, 2);//타인의 게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}", post.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("postImageList[0].id").exists())
                .andExpect(jsonPath("postImageList[0].post.id").exists())
                .andExpect(jsonPath("postImageList[0].uri").exists())
                .andExpect(jsonPath("postTagList[0].id").exists())
                .andExpect(jsonPath("postTagList[0].post.id").exists())
                .andExpect(jsonPath("postTagList[0].tag").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").value(post.getViewCount() + 1))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-post-comments").exists())
                .andExpect(jsonPath("_links.get-posts").exists())
                //타인의 게시물 조회시에는 수정, 삭제 링크 제공 x
                .andExpect(jsonPath("_links.update-post").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-post").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("post 게시물 하나 조회 실패-존재하지 않는 리소스 조회(404 Not found))")
    public void getPostFail_Not_Found() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);
        createPost(account, 0, 2, 2);//게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정")
    public void updatePost() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //mock image part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostDto(0, 5);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/" + post.getId())
                .part(part)
                .file(mockFile)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("article").exists())
                .andExpect(jsonPath("postTagList[0].id").exists())
                .andExpect(jsonPath("postTagList[0].post.id").exists())
                .andExpect(jsonPath("postTagList[0].tag").exists())
                .andExpect(jsonPath("postImageList[0].id").exists())
                .andExpect(jsonPath("postImageList[0].post.id").exists())
                .andExpect(jsonPath("postImageList[0].uri").exists())
                .andExpect(jsonPath("location").exists())
                .andExpect(jsonPath("latitude").exists())
                .andExpect(jsonPath("longitude").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-posts").exists())
                .andExpect(jsonPath("_links.delete-post").exists())
                .andDo(document("update-post",
                        links(
                                linkWithRel("self").description("수정된 post 게시물의 리소스 링크"),
                                linkWithRel("get-posts").description("post 게시물 리스트를 조회할 수 있는 링크"),
                                linkWithRel("delete-post").description("수정된 post 게시물을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestParts(
                                partWithName("imageFiles").description("수정할 이미지 파일 리스트"),
                                partWithName("post").description("post게시물의 dto json")
                        ),
                        requestPartFields(
                                "post",
                                fieldWithPath("article").description("게시물 본문"),
                                fieldWithPath("location").description("등록된 장소"),
                                fieldWithPath("latitude").description("장소의 위도"),
                                fieldWithPath("longitude").description("장소의 경도"),
                                fieldWithPath("tagList[].tag").description("게시물에 달 태그")
                        ),
                        requestHeaders,
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("post 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자 id"),
                                fieldWithPath("account.nickname").description("게시물 작성자 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자 프로필 이미지 경로"),
                                fieldWithPath("article").description("게시물의 본문"),
                                fieldWithPath("postTagList[].id").description("게시물에 붙은 태그의 id"),
                                fieldWithPath("postTagList[].post.id").description("태그가 붙은 게시물의 id"),
                                fieldWithPath("postTagList[].tag").description("태그"),
                                fieldWithPath("postImageList[].id").description("게시물의 이미지 id"),
                                fieldWithPath("postImageList[].post.id").description("게시물의 id"),
                                fieldWithPath("postImageList[].uri").description("이미지의 uri"),
                                fieldWithPath("location").description("게시물에 등록한 장소명"),
                                fieldWithPath("latitude").description("장소의 위도"),
                                fieldWithPath("longitude").description("장소의 경도"),
                                fieldWithPath("regDate").description("게시물 작성 시간"),
                                fieldWithPath("viewCount").description("조회수"),
                                fieldWithPath("_links.self.href").description("수정된 post 게시물 리소스 링크"),
                                fieldWithPath("_links.get-posts.href").description("post 게시물 리스트를 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-post.href").description("수정된 post 게시물을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    private MockMultipartFile createMockMultipartFile() throws IOException {
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        return new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());
    }

    @Test
    @DisplayName("post 게시물 수정 실패-post part가 없는 경우(400 Bad request)")
    public void updatePostFail_Empty_Post_Part() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //mock file
        MockMultipartFile mockFile = createMockMultipartFile();

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-post part에 허용되지 않은 값이 포함된 경우(400 Bad request)")
    public void updatePostFail_Wrong_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        Post postDto = createPostWithUnknowValue();//허용되지 않는 값이 포함된 dto
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-post part의 값이 비즈니스 로직에 맞지 않는 경우(400 Bad request)")
    public void updatePostFail_Unknown_property() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-이미지 파일이 하나도 안 넘어온 경우(400 Bad request)")
    public void updatePostFail_No_Image_File() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-이미지 파일이 10개를 초과한 경우(400 Bad request)")
    public void updatePostFail_Exceed_Max_Image_Count() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-이미지 파일이 아닌 경우(415 Unsupported media type)")
    public void updatePostFail_Not_Image_File() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFiles", "test.txt", "text/plain", "This is not a image file.".getBytes());

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-oauth 인증을 하지 않은 경우(401 Unauthorized)")
    public void updatePostFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 1);

        Post post = createPost(account, 0, 2, 2);//게시물 생성

        //이미지 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post 게시물 수정 실패-타인의 게시물을 수정하려고 하는 경우(403 Forbidden)")
    public void updatePost_Forbidden() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(otherAccount, 0, 2, 2);//게시물 생성

        //mock image part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostDto(0, 5);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", post.getId())
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("post 게시물 수정 실패-존재하지 않는 게시물을 수정하려고 하는 경우(404 Not found)")
    public void updatePost_Not_Found() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        //mock image part
        MockMultipartFile mockFile = createMockMultipartFile();

        //post part
        PostDto postDto = createPostDto(0, 5);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts/{postId}", 404)
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("post 게시물 삭제")
    public void deletePost() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        Post post = createPost(account, 0, 2, 2);//post게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-post",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("삭제할 post 게시물 id")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("post 게시물 삭제 실패-인증하지 않은 상태에서 삭제(401 Unauthorized)")
    public void deletePostFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);

        Post post = createPost(account, 0, 2, 2);//post게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}", post.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("post 게시물 삭제-다른 사용자의 게시물을 삭제하려고 하는 경우(403 Forbidden)")
    public void deletePostFail_Forbidden() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Post post = createPost(otherAccount, 0, 2, 2);//다른 사용자의 post게시물 하나 생성

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}", post.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("post 게시물 삭제-존재하지 않는 게시물을 삭제하려고 하는 경우(404 Not found)")
    public void deletePostFail_Not_Found() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }


    private PostDto createPostDto(int index, int tagCount) {

        Set<PostTagDto> postTagDtos = new LinkedHashSet<>();
        IntStream.range(0, tagCount).forEach(i -> {
            PostTagDto postTagDto = createPostTagDto(i);
            postTagDtos.add(postTagDto);
        });

        return PostDto.builder()
                .article("This is article" + index)
                .postTags(postTagDtos)
                .location("somewhere" + index)
                .latitude(33.0000)
                .longitude(127.0000)
                .build();
    }

    private PostTagDto createPostTagDto(int index) {
        return PostTagDto.builder()
                .tag("tag" + index)
                .build();
    }

    private Post createPostWithUnknowValue() {
        return Post.builder()
                .id(1)//허용되지 않은 값
                .article("This is article")
                .location("somewhere")
                .latitude(33.0000)
                .longitude(127.0000)
                .regDate(LocalDateTime.now())
                .viewCount(100)
                .build();
    }

    private PostDto createPostWithWrongValue() {
        return PostDto.builder()
                .article("This is article")
                //location, latitude, longitude는 셋 다 null이거나 셋 다 값이 있어야만 한다.
                .location(null)
                .latitude(33.0000)
                .longitude(127.0000)
                .build();
    }


}