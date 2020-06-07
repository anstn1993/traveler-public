package me.moonsoo.travelerrestapi.post;

import io.findify.s3mock.S3Mock;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource({"classpath:/aws.properties", "classpath:/application-test.properties"})
class PostControllerTest extends BaseControllerTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostImageRepository postImageRepository;

    @Autowired
    PostTagRepository postTagRepository;

    @AfterAll
    public static void closeMockS3Server(@Autowired S3Mock s3Mock) {
        s3Mock.stop();
    }

    @AfterEach
    public void setUp() {
        postImageRepository.deleteAll();
        postTagRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("post 게시물 추가 테스트")
    public void createPost() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        //이미지 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile1 = new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        //post part
        PostDto postDto = createPostDto(0, 5);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(multipart("/api/posts")
                .part(part)
                .file(mockFile1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
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
                ))
        ;
    }


    @Test
    @DisplayName("post 게시물 추가 실패-post part가 없는 경우(400 Bad request)")
    public void createPostFail_Empty_Post_Part() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);


        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile1 = new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-post part에 허용되지 않은 값이 담기는 경우(400 bad request)")
    public void createPostFail_Unknown_property() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        //이미지 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile1 = new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        //post part
        Post postDto = createPostWithUnknowValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile1)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-post part에 비즈니스 로직에 맞지 않은 값이 담기는 경우(400 bad request)")
    public void createPostFail_Wrong_Value() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        //이미지 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile1 = new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        //post part
        PostDto postDto = createPostWithWrongValue();
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile1)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 하나도 안 넘어온 경우(400 bad request)")
    public void createPostFail_No_Image_File() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 10개를 초과하는 경우(400 Bad request)")
    public void createPostFail_Exceed_Max_Image_Count() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        //이미지 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFiles", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

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
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("post 게시물 추가 실패-이미지 파일이 아닌 경우 400(bad request)")
    public void createPostFail_Not_Image_File() throws Exception {
        //Given
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);


        //이미지 파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFiles", "test.txt", "text/plain", "This is not a image file.".getBytes());


        //post part
        PostDto postDto = createPostDto(0, 3);
        MockPart part = new MockPart("post", "post", objectMapper.writeValueAsString(postDto).getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/posts")
                .file(mockFile)
                .part(part)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }



    private Post createPost(Account account, int index, int tagCount) {
        Post post = Post.builder()
                .account(account)
                .article("This is article" + index)
                .location("somewhere" + index)
                .latitude(33.0000)
                .longitude(127.0000)
                .build();

        Set<PostTag> postTags = new LinkedHashSet<>();
        IntStream.range(0, tagCount).forEach(i -> {
            PostTag postTag = createPostTag(i, post);
            postTags.add(postTag);
        });

        return post;
    }

    private PostTag createPostTag(int index, Post post) {
        return PostTag.builder()
                .post(post)
                .tag("tag" + index)
                .id(index)
                .build();
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