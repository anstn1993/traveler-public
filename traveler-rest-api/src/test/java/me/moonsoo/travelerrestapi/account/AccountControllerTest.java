package me.moonsoo.travelerrestapi.account;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import io.findify.s3mock.S3Mock;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.email.EmailService;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.FileCopyUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EmailService emailService;

    @Autowired
    AmazonS3 amazonS3;

    @Autowired
    S3Properties s3Properties;

    @Autowired
    PasswordEncoder passwordEncoder;

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
    public static SmtpServerExtension smtpServerExtension = new SmtpServerExtension(new GreenMail(ServerSetup.SMTP));


    @Test
    @DisplayName("사용자 추가 테스트(프로필 이미지 o)")
    public void createAccount_With_Profile_Image() throws Exception {

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

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
                                linkWithRel("get-accounts").description("account 리소스를 조회할 수 있는 링크"),
                                linkWithRel("update-account").description("생성된 account 리소스를 수정할 수 있는 링크"),
                                linkWithRel("delete-account").description("생성된 acocunt 리소스를 삭제할 수 있는 링크")
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
                                fieldWithPath("id").description("생성된 사용자 id"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("profileImageUri").description("사용자의 프로필 이미지 경로"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("nickname").description("사용자 닉네임"),
                                fieldWithPath("sex").description("사용자 성별"),
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
        MockMultipartFile mockFile = createMockMultipartFile();

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
    public void createAccountFail_Empty_Account_Part() throws Exception {
        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

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
        MockMultipartFile mockFile = createMockMultipartFile();

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
        MockMultipartFile mockFile = createMockMultipartFile();

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

    @Test
    @DisplayName("인증 상태에서 사용자 목록 조회(totalElement=30, size=10, page=1)")
    public void getAccounts_With_Auth() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);
        IntStream.range(1, 31).forEach(i -> {
            createAccount(email, password, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
        ;
    }

    @ParameterizedTest(name = "{index} => filter = {0}, search = {1}")
    @MethodSource("filterAndSearchProvider")
    @DisplayName("인증 상태에서 사용자 목록 조회(totalElement=30, size=10, page=0)")
    public void getAccounts_With_Auth_And_Filter(String filter, String search) throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);
        IntStream.range(1, 31).forEach(i -> {
            createAccount(email, password, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC")
                .param("filter", filter)
                .param("search", search))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
        ;
    }

    @Test
    @DisplayName("이메일 인증이 안 된 사용자 제외")
    public void getNotEmailAuthAccounts() throws Exception {
        String email = "user@email.com";
        String password = "user";
        IntStream.range(0, 30).forEach(i -> {
            createAccountWithoutEmailAuth(email, password, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").doesNotHaveJsonPath())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @ParameterizedTest(name = "{index} => filter = {0}, search = {1}")
    @MethodSource("filterAndSearchProvider")
    @DisplayName("미인증 상태에서 사용자 목록 조회(totalElement=30, size=10, page=0)")
    public void getAccounts_Without_Auth(String filter, String search) throws Exception {
        String email = "user@email.com";
        String password = "user";
        IntStream.range(1, 31).forEach(i -> {
            createAccount(email, password, i);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,ASC")
                .param("filter", filter)
                .param("search", search))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.accountList").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-account").exists())
                .andDo(document("get-accounts",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-account").description("사용자 추가 링크(미인증 상태에서 요청한 경우 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id)"),
                                parameterWithName("filter").optional().description("검색어 필터(name-이름, nickname-닉네임)"),
                                parameterWithName("search").optional().description("검색어")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responsePageFields.and(
                                fieldWithPath("_embedded.accountList[].id").description("사용자 id"),
                                fieldWithPath("_embedded.accountList[].email").description("사용자 email"),
                                fieldWithPath("_embedded.accountList[].profileImageUri").description("사용자 프로필 이미지 경로"),
                                fieldWithPath("_embedded.accountList[].name").description("사용자 이름"),
                                fieldWithPath("_embedded.accountList[].nickname").description("사용자 닉네임"),
                                fieldWithPath("_embedded.accountList[].sex").description("사용자 성별"),
                                fieldWithPath("_embedded.accountList[]._links.self.href").description("해당 사용자 조회 링크"),
                                fieldWithPath("_links.create-account.href").description("사용자 추가 링크(미인증 상태에서 요청한 경우 활성화)")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 자기 자신 조회")
    public void getMyAccount_With_Auth() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
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
                .andExpect(jsonPath("_links.create-account-following").doesNotHaveJsonPath())
                .andDo(document("get-my-account",
                        links(
                                linkWithRel("self").description("조회한 사용자 리소스 조회 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accounts").description("사용자 목록 조회 링크"),
                                linkWithRel("update-account").description("사용자 리소스 수정 링크(인증상태에서 자신을 조회한 경우에 활성화)"),
                                linkWithRel("delete-account").description("사용자 리소스 삭제 링크(인증상태에서 자신을 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("사용자 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("조회한 사용자 id"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("profileImageUri").description("사용자의 프로필 이미지 경로"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("nickname").description("사용자 닉네임"),
                                fieldWithPath("sex").description("사용자 성별"),
                                fieldWithPath("_links.self.href").description("조회한 account 리소스 링크"),
                                fieldWithPath("_links.get-accounts.href").description("account 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-account.href").description("사용자 리소스 수정 링크(인증상태에서 자신을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-account.href").description("사용자 리소스 삭제 링크(인증상태에서 자신을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 다른 사용자 조회")
    public void getOtherAccount_With_Auth() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}", otherAccount.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").doesNotExist())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.update-account").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-account").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.create-account-following").exists())
                .andDo(document("get-other-account",
                        links(
                                linkWithRel("self").description("조회한 사용자 리소스 조회 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accounts").description("사용자 목록 조회 링크"),
                                linkWithRel("create-account-following").description("조회한 사용자를 팔로우할 수 있는 링크(인증상태에서 다른 사용자를 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("사용자 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("생성된 사용자 id"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("profileImageUri").description("사용자의 프로필 이미지 경로"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("nickname").description("사용자 닉네임"),
                                fieldWithPath("sex").description("사용자 성별"),
                                fieldWithPath("_links.self.href").description("생성된 account 리소스 링크"),
                                fieldWithPath("_links.get-accounts.href").description("account 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.create-account-following.href").description("조회한 사용자를 팔로우할 수 있는 링크(인증상태에서 다른 사용자를 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 사용자 조회")
    public void getAccount_Without_Auth() throws Exception {
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(email, password, 1);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}", account.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").doesNotExist())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.update-account").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-account").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.create-account-following").doesNotHaveJsonPath())
        ;
    }

    @Test
    @DisplayName("이메일 인증을 하지 않은 사용자 조회")
    public void getNotEmailAuthAccount_Without_Auth() throws Exception {
        String email = "user@email.com";
        String password = "user";
        Account account = createAccountWithoutEmailAuth(email, password, 1);//이메일 인증을 하지 않은 사용자

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}", account.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("사용자 조회 실패-존재하지 않는 사용자(404 Not found)")
    public void getAccountFail_Not_Found() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/accounts/{accountId}", 404)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정(기존 프로필 이미지 o, 새로운 프로필 이미지 o)")
    public void updateAccount_Change_Profile_Image() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthTokenForAccountWithProfileImage(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").exists())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andDo(document("update-account",
                        links(
                                linkWithRel("self").description("수정된 account 리소스 링크"),
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("get-accounts").description("account 리소스를 조회할 수 있는 링크"),
                                linkWithRel("delete-account").description("수정된 acocunt 리소스를 삭제할 수 있는 링크")
                        ),
                        requestParts(
                                partWithName("imageFile").description("수정할할 프로필 이미지 파일"),
                                partWithName("account").description("account의 dto json")
                        ),
                        requestPartFields(
                                "account",
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("sex").description("성별")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 컨텐츠 타입")
                        ),
                        responseHeaders,
                        responseFields(
                                fieldWithPath("id").description("생성된 사용자 id"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("profileImageUri").description("사용자의 프로필 이미지 경로"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("nickname").description("사용자 닉네임"),
                                fieldWithPath("sex").description("사용자 성별"),
                                fieldWithPath("_links.self.href").description("수정된 account 리소스 링크"),
                                fieldWithPath("_links.get-accounts.href").description("account 목록을 조회할 수 있는 링크"),
                                fieldWithPath("_links.delete-account.href").description("수정된 account 리소스를 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
                .andReturn();

        Jackson2JsonParser parser = new Jackson2JsonParser();
        Map<String, Object> contentJson = parser.parseMap(mvcResult.getResponse().getContentAsString());
        String profileImageUri = (String) contentJson.get("profileImageUri");
        assertThat(profileImageUri).isNotEqualTo(account.getProfileImageUri());//새로운 프로필 이미지가 등록되었기 때문에 기존 프로필 이미지 uri와 달라야 한다.
    }

    @Test
    @DisplayName("사용자 리소스 수정(기존 프로필 이미지 o, 새로운 프로필 이미지 x)")
    public void updateAccount_Remove_Former_Profile_Image() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthTokenForAccountWithProfileImage(email, password, 0);//프로필 이미지가 이미 있는 사용자의 access token

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").doesNotExist())//프로필 이미지 uri가 존재하지 않아야 한다.
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정(기존 프로필 이미지 x, 새로운 프로필 이미지 o)")
    public void updateAccount_Enroll_New_Profile_Image() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 없는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").exists())//프로필 이미지의 uri가 존재해야 한다.
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정(기존 프로필 이미지 x, 새로운 프로필 이미지 x)")
    public void updateAccount_Without_Profile_Image() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 이미지가 없는 사용자의 access token

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("profileImageUri").doesNotExist())//프로필 이미지 uri가 존재하지 않아야 한다.
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-프로필 이미지가 두 개 이상(400 Bad request)")
    public void updateAccount_Exceed_Max_Image_Count() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                //이미지 파일 2개
                .file(mockFile)
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-이미지 파일이 아닌 경우(400 Bad request)")
    public void updateAccount_Not_Image_File() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //파일 part
        MockMultipartFile mockFile = new MockMultipartFile("imageFile", "test.txt", "text/plain", "This is not a image file.".getBytes());

        //account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-account part가 존재하지 않는 경우(400 Bad request)")
    public void updateAccount_Empty_Account_Part() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-account part에 허용되지 않은 값이 포함(400 Bad request)")
    public void updateAccount_Unknown_Value() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        Account accountDto = createAccountDtoWithUnknownValue();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }


    @Test
    @DisplayName("사용자 리소스 수정 실패-account part의 값이 비즈니스 로직에 맞지 않은 경우(400 Bad request)")
    public void updateAccount_Wrong_Value() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        AccountDtoForUpdate accountDto = createAccountDtoForUpdateWithWrongValue();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void updateAccount_Unauthorized() throws Exception {
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(email, password, 0);

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + account.getId())
                .file(mockFile)
                .part(accountPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-다른 사용자 리소스를 수정하려고 하는 경우(403 Forbidden)")
    public void updateAccount_Forbidden() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token
        Account otherAccount = createAccount(email, password, 1);//다른 사용자
        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //Account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + otherAccount.getId())
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("사용자 리소스 수정 실패-존재하지 않는 사용자 리소스(404 Not found)")
    public void updateAccount_Not_Found() throws Exception {
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(email, password, 0);//프로필 사진이 이미 있는 사용자의 access token

        //프로필 사진 파일 part
        MockMultipartFile mockFile = createMockMultipartFile();

        //Account part
        AccountDtoForUpdate accountDto = createAccountDtoForUpdate();
        MockPart accountPart = new MockPart("account", "account", objectMapper.writeValueAsString(accountDto).getBytes());
        accountPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/accounts/" + 404)
                .file(mockFile)
                .part(accountPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }


    //매개변수 테스트의 인자를 생성해주는 메소드
    private static Stream<Arguments> filterAndSearchProvider() {
        return Stream.of(
                Arguments.of("nickname", "user"),
                Arguments.of("name", "user")
        );
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

    private AccountDtoForUpdate createAccountDtoForUpdateWithWrongValue() {
        return AccountDtoForUpdate.builder()
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

    //account 리소스 수정시 요청 dto
    private AccountDtoForUpdate createAccountDtoForUpdate() {
        return AccountDtoForUpdate.builder()
                .name("user")
                .password("11111111")
                .nickname("user")
                .sex(Sex.MALE)
                .build();
    }

    //이메일 인증이 이루어지지 않은 계정 생성
    private Account createAccountWithoutEmailAuth(String email, String password, int index) {
        //Given
        Account account = Account.builder()
                .email(index + email)
                .password(password)
                .name("user" + index)
                .nickname("user" + index)
                .emailAuth(false)
                .profileImageUri(null)
                .regDate(LocalDateTime.now())
                .authCode("authcode")
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();
        return accountAuthService.saveAccount(account);
    }

    private String uploadProfileImage(Account account) throws IOException {
        String targetDirectory = s3Properties.getProfileImageDirectory();//이미지를 저장할 디렉토리
        //이미지 파일
        String originalFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        File originalFile = resourceLoader.getResource("classpath:image/" + originalFileName).getFile();
        String imageFileName = account.getId() + new SimpleDateFormat("HHmmss").format(new Date()) + ".jpg";
        //로컬에 임시 이미지 파일 생성
        File tempFile = new File(imageFileName);
        if (tempFile.createNewFile()) {
            FileCopyUtils.copy(originalFile, tempFile);
        }
        amazonS3.putObject(new PutObjectRequest(s3Properties.getBUCKET(), targetDirectory + "/" + tempFile.getName(), tempFile).withCannedAcl(CannedAccessControlList.PublicRead));//mock s3 bucket에 파일 저장
        tempFile.delete();//로컬에 임시 파일 삭제
        return amazonS3.getUrl(s3Properties.getBUCKET(), targetDirectory + "/" + tempFile.getName()).toString();
    }

    private Account createAccountWithProfileImage(String email, String password, int index) throws IOException {

        //Given
        Account account = Account.builder()
                .email(index + email)
                .password(passwordEncoder.encode(password))
                .name("user" + index)
                .nickname("user" + index)
                .emailAuth(true)
                .profileImageUri(null)
                .regDate(LocalDateTime.now())
                .authCode(passwordEncoder.encode("authcode"))
                .sex(Sex.MALE)
                .roles(Set.of(AccountRole.USER))
                .build();
        Account savedAccount = accountRepository.save(account);
        String uri = uploadProfileImage(savedAccount);
        savedAccount.setProfileImageUri(uri);
        return savedAccount;
    }

    //인자로 들어가는 account는 save된 상태
    protected String getAuthTokenForAccountWithProfileImage(String email, String password, int index) throws Exception {
        //Given
        String clientId = "traveler";
        String clientPassword = "pass";
        account = createAccountWithProfileImage(email, password, index);

        String contentAsString = mockMvc.perform(post("/oauth/token").with(httpBasic(clientId, clientPassword))
                .param("username", index + email)
                .param("password", password)
                .param("grant_type", "password"))
                .andReturn().getResponse().getContentAsString();

        Jackson2JsonParser parser = new Jackson2JsonParser();
        return (String) parser.parseMap(contentAsString).get("access_token");
    }

    private MockMultipartFile createMockMultipartFile() throws IOException {
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        return new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());
    }

}