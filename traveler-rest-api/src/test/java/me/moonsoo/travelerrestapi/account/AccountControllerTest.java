package me.moonsoo.travelerrestapi.account;

import com.amazonaws.services.s3.AmazonS3;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import io.findify.s3mock.S3Mock;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.config.MockS3Config;
import me.moonsoo.travelerrestapi.email.EmailService;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.test.context.TestPropertySource;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EmailService emailService;

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
    public void setUp() {
        accountRepository.deleteAll();
    }

    @RegisterExtension
    public static SmtpServerExtension smtpServerExtension = new SmtpServerExtension(new GreenMail(ServerSetup.ALL));


    @Test
    @DisplayName("사용자 추가 테스트(프로필 이미지 o)")
    public void createAccount_With_Profile_Image() throws Exception {

        //프로필 사진 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        //account part
        AccountDto accountDto = createAccountDto();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        //smtp서버 유저 set
        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(accountDto.getEmail(), "1111");

        mockMvc.perform(multipart("/api/accounts")
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").exists())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.update-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andDo(document("create-account",
                        links(
                                linkWithRel("self").description("생성된 account 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accounts").description("account 리소트를 조회할 수 있는 링크"),
                                linkWithRel("update-account").description("생성된 account를 수정할 수 있는 링크"),
                                linkWithRel("delete-account").description("생성된 acocunt를 삭제할 수 있는 링크")
                        ),
                        requestParts(
                                partWithName("imageFile").description("업로드할 프로필 이미지 파일"),
                                partWithName("account").description("account의 dto json")
                        ),
                        requestPartFields(
                                "account",
                                fieldWithPath("email").description("계정 메일(실제 이메일 인증에 사용되는 메일)"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("sex").description("성별")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 컨텐츠 타입")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("생성된 account 리소스 링크")
                        ),
                        responseFields(
                                fieldWithPath("id").description("post 게시물의 id"),
                                fieldWithPath("email").description("게시물 작성자 id"),
                                fieldWithPath("profileImageUri").description("게시물의 본문"),
                                fieldWithPath("name").description("게시물에 붙은 태그의 id"),
                                fieldWithPath("nickname").description("태그가 붙은 게시물의 id"),
                                fieldWithPath("sex").description("태그"),
                                fieldWithPath("_links.self.href").description("생성된 account 리소스 링크"),
                                fieldWithPath("_links.get-accounts.href").description("account 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-account.href").description("생성된 account를 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-account.href").description("생성된 account를 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("사용자 추가 테스트(프로필 이미지 x)")
    public void createAccount_Without_Profile_Image() throws Exception {
        //account part
        AccountDto accountDto = createAccountDto();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        //smtp서버 유저 set
        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(accountDto.getEmail(), "1111");

        mockMvc.perform(multipart("/api/accounts")
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").doesNotExist())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.update-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
        ;
    }

    @Test
    @DisplayName("사용자 추가 테스트 실패-이미지 파일이 2개 이상인 경우(400 Bad request)")
    public void createAccountFail_Exceed_Max_Image_Count() throws Exception {
        //프로필 사진 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        //account part
        AccountDto accountDto = createAccountDto();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts")
                //이미지 파일 2개
                .file(mockFile)
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 추가 테스트 실패-이미지 파일이 아닌 경우(400 Bad request)")
    public void createAccountFail_Not_Image_File() throws Exception {
        //파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", "test.txt", "text/plain", "This is not a image file.".getBytes());

        //account part
        AccountDto accountDto = createAccountDto();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts")
                //이미지 파일 2개
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 추가 테스트 실패-account part가 없는 경우(400 Bad request)")
    public void createAccountFail_Empty_Post_Part() throws Exception {
        //프로필 사진 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        mockMvc.perform(multipart("/api/accounts")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 추가 테스트 실패-account part에 허용되지 않은 값이 있는 경우(400 Bad request)")
    public void createAccountFail_Unknown_Value() throws Exception {
        //프로필 사진 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        Account accountDto = createAccountDtoWithUnknownValue();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts")
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }


    @Test
    @DisplayName("사용자 추가 테스트 실패-account part에 비즈니스 로직에 맞지 않는 값이 있는 경우(400 Bad request)")
    public void createAccountFail_Wrong_Value() throws Exception {
        //프로필 사진 파일 part
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());

        AccountDto accountDto = createAccountDtoWithWrongValue();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts")
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("인증 이메일 전송 테스트")
    public void sendEmail() throws MessagingException {
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(email, password, 0);

        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(account.getEmail(), account.getPassword());

        emailService.sendAuthMessage(account);//인증 메일 전송

        MimeMessage[] receivedMessages = smtpServerExtension.getMessages();
        assertAll("email test", () -> {
            assertEquals(receivedMessages.length, 1);
            assertEquals(receivedMessages[0].getSubject(), "traveler 이용을 위한 인증 메일 입니다.");
            assertEquals(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0], new InternetAddress(account.getEmail()));
        });
    }


    //비즈니스 로직에 맞지 않는 account dto 생성
    private AccountDto createAccountDtoWithWrongValue() {
        return AccountDto.builder()
                .email(null)
                .name(null)
                .password(null)
                .nickname(null)
                .sex(null)
                .build();
    }

    //허용되지 않은 값이 포함된 account dto 생성
    private Account createAccountDtoWithUnknownValue() {
        return Account.builder()
                .email("user@email.com")
                .name("user")
                .password("user")
                .nickname("user")
                .sex(Sex.MALE)
                //허용되지 않은 값들
                .regDate(LocalDateTime.now())
                .authCode("adfaef")
                .emailAuth(true)
                .id(3000)
                .profileImageUri(null)
                .roles(Set.of(AccountRole.USER))
                .build();
    }

    //정상적인 account dto 생성
    private AccountDto createAccountDto() {
        return AccountDto.builder()
                .email("anstn1993@gmail.com")
                .name("user")
                .password("11111111")
                .nickname("user")
                .sex(Sex.MALE)
                .build();
    }

}