package me.moonsoo.travelerapplication.account;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerapplication.BaseControllerTest;
import me.moonsoo.travelerapplication.email.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EmailService emailService;

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @RegisterExtension
    public static SmtpServerExtension smtpServerExtension = new SmtpServerExtension(new GreenMail(new ServerSetup(26, null, "smtp")));

    @Test
    @DisplayName("미인증 상태에서 로그인 페이지 요청")
    public void getLoginPage_Without_Auth() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/login"))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 로그인 페이지 요청")
    public void getLoginPage_With_Auth() throws Exception {
        //사용자 추가
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "11111111";
        Account account = createAccount(username, email, password, 0);
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);

        //세션에 사용자 정보를 넣어준다.
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("account", sessionAccount);

        mockMvc.perform(get("/login")
                .session(mockHttpSession))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 아이디 찾기 페이지 요청")
    public void getFindUsernamePage_Without_Auth() throws Exception {
        mockMvc.perform(get("/find-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-username"))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 아이디 찾기 페이지 요청")
    public void getFindUsernamePage_With_Auth() throws Exception {
        //사용자 추가
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "11111111";
        Account account = createAccount(username, email, password, 0);
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);

        //세션에 사용자 정보를 넣어준다.
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("account", sessionAccount);

        mockMvc.perform(get("/find-username")
                .session(mockHttpSession))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 비밀번호 찾기 페이지 요청")
    public void getFindPasswordPage_Without_Auth() throws Exception {
        mockMvc.perform(get("/find-password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 비밀번호 찾기 페이지 요청")
    public void getFindPasswordPage_With_Auth() throws Exception {
        //사용자 추가
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "11111111";
        Account account = createAccount(username, email, password, 0);
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);

        //세션에 사용자 정보를 넣어준다.
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("account", sessionAccount);

        mockMvc.perform(get("/find-password")
                .session(mockHttpSession))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
        ;
    }

    @Test
    @DisplayName("인증 이메일 전송 테스트")
    public void sendEmail() throws MessagingException, IOException {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(username, email, password, 0);

        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(account.getEmail(), account.getPassword());

        emailService.sendAuthMessage(account);//인증 메일 전송

        MimeMessage[] receivedMessages = smtpServerExtension.getMessages();
        assertAll("email test", () -> {
            assertEquals(receivedMessages.length, 1);
            assertEquals(receivedMessages[0].getSubject(), "traveler 아이디/비밀번호 찾기를 위한 인증 메일 입니다.");
            assertEquals(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0], new InternetAddress(account.getEmail()));
        });
    }

    @Test
    @DisplayName("아이디 찾기 테스트")
    public void authenticate_Find_Username() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(username, email, password, 0);

        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(account.getEmail(), account.getPassword());

        //본인 인증을 위해 사용자 이름과 이메일을 입력한 후 post요청
        MvcResult createAuthCodeResult = mockMvc.perform(post("/find-username")
                .with(csrf())
                .param("name", account.getName())
                .param("email", account.getEmail()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/authenticate"))
                .andReturn();

        HttpSession session = createAuthCodeResult.getRequest().getSession();
        MimeMessage[] receivedMessages = smtpServerExtension.getMessages();
        String content = (String) receivedMessages[0].getContent();
        String authCode = content.replace("인증번호: ", "");//인증 번호

        assertAll("email auth test",
                () -> {
                    assertThat(session.getAttribute("authCode")).isEqualTo(authCode);
                    assertThat(session.getAttribute("username")).isEqualTo(account.getUsername());
                });

        MockHttpSession authCodeMockSession = new MockHttpSession();
        authCodeMockSession.setAttribute("authCode", authCode);

        //이메일로 전송된 인증번호를 담아서 post요청해서 본인 인증을 하는 요청
        mockMvc.perform(post("/authenticate")
                .session(authCodeMockSession)
                .with(csrf())
                .param("authCode", authCode))
                .andDo(print())
                .andExpect(status().isOk())
        ;


        MockHttpSession usernameMockSession = new MockHttpSession();
        usernameMockSession.setAttribute("username", username);
        //본인 인증 후 아이디를 확인하는 요청
        MvcResult findUsernameResult = mockMvc.perform(get("/find-username/result")
                .session(usernameMockSession)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(account.getUsername())))
                .andReturn();

        HttpSession findUsernameSession = findUsernameResult.getRequest().getSession();
        assertAll("session invalid test", () -> {
            assertThat(findUsernameSession.getAttribute("authCode")).isNull();
            assertThat(findUsernameSession.getAttribute("authType")).isNull();
            assertThat(findUsernameSession.getAttribute("username")).isNull();
        });
    }

    @Test
    @DisplayName("비밀번호 찾기 본인 인증")
    public void authenticate_Find_Password() throws Exception {
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        Account account = createAccount(username, email, password, 0);

        smtpServerExtension.getGreenMail().setUser("mansoo@localhost", "1111");
        smtpServerExtension.getGreenMail().setUser(account.getEmail(), account.getPassword());

        //본인 인증을 위해 사용자 이름과 이메일을 입력한 후 post요청
        MvcResult createAuthCodeResult = mockMvc.perform(post("/find-password")
                .with(csrf())
                .param("username", account.getUsername())
                .param("email", account.getEmail()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/authenticate"))
                .andReturn();

        HttpSession session = createAuthCodeResult.getRequest().getSession();
        MimeMessage[] receivedMessages = smtpServerExtension.getMessages();
        String content = (String) receivedMessages[0].getContent();
        String authCode = content.replace("인증번호: ", "");//인증 번호

        assertAll("email auth test",
                () -> {
                    assertThat(session.getAttribute("authCode")).isEqualTo(authCode);
                    assertThat(session.getAttribute("username")).isEqualTo(account.getUsername());
                });

        MockHttpSession authCodeMockSession = new MockHttpSession();
        authCodeMockSession.setAttribute("authCode", authCode);

        //이메일로 전송된 인증번호를 담아서 post요청해서 본인 인증을 하는 요청
        mockMvc.perform(post("/authenticate")
                .session(authCodeMockSession)
                .with(csrf())
                .param("authCode", authCode))
                .andDo(print())
                .andExpect(status().isOk())
        ;


        MockHttpSession passwordMockSession = new MockHttpSession();
        passwordMockSession.setAttribute("username", username);
        passwordMockSession.setAttribute("authCode", authCode);
        //본인 인증 후 아이디를 확인하는 요청
        MvcResult findPasswordResult = mockMvc.perform(get("/find-password/result")
                .session(passwordMockSession)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        HttpSession findPasswordSession = findPasswordResult.getRequest().getSession();
        assertAll("session invalid after find username auth complete test",
                () -> {
                    assertThat(findPasswordSession.getAttribute("authCode")).isNull();
                    assertThat(findPasswordSession.getAttribute("authType")).isNull();
                });

        //비밀번호 변경 post요청
        mockMvc.perform(post("/find-password/result")
                .session(passwordMockSession)
                .param("password", account.getPassword())
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("회원가입(프로필 이미지 o)")
    public void signUp() throws Exception {

        //profile image file
        MockMultipartFile imageFile = createMockMultipartFile();

        //request params set
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "anstn1993");
        params.add("password", "11111111");
        params.add("password-check", "11111111");
        params.add("email", "test@localhost.com");
        params.add("name", "김문수");
        params.add("nickname", "만수");
        params.add("sex", "MALE");


        mockMvc.perform(multipart("/sign-up")
                .file(imageFile)
                .params(params))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("username").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("profileImageUri").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("nickname").exists())
                .andExpect(jsonPath("sex").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-accounts").exists())
                .andExpect(jsonPath("_links.update-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
        ;

        Optional<Account> accountOpt = accountRepository.findByUsername("anstn1993");
        assertThat(accountOpt.isPresent()).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패-요청 parameter가 다 넘어오지 않은 경우(400 Bad request)")
    public void signUpFail_Not_Enough_Value() throws Exception {
        //request params set
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "anstn1993");
        params.add("password", "11111111");
        params.add("password-check", "11111111");
        params.add("email", "test@localhost.com");
        params.add("name", "김문수");
        //닉네임과 성별을 request param에서 제외
//        params.add("nickname", "만수");
//        params.add("sex", "MALE");


        mockMvc.perform(multipart("/sign-up")
                .params(params))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

        Optional<Account> accountOpt = accountRepository.findByUsername("anstn1993");
        assertThat(accountOpt.isEmpty()).isTrue();
    }

    @ParameterizedTest(name = "{index} => params = {0}")
    @MethodSource("invalidRequestParamProvider")
    @DisplayName("회원가입 실패-유효하지 않은 회원가입 폼 데이터가 request param에 담긴 경우(400 Bad request)")
    public void signUpFail_Invalid_Request_params(MultiValueMap<String, String> params) throws Exception {
        mockMvc.perform(multipart("/sign-up")
                .params(params))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

        Optional<Account> accountOpt = accountRepository.findByUsername("anstn1993");
        assertThat(accountOpt.isEmpty()).isTrue();
    }

    public static Stream<Arguments> invalidRequestParamProvider() {
        return Stream.of(
                Arguments.of(createInvalidParams("ans", "11111111", "11111111", "email@email.com", "김문수", "만수", "MALE")),
                Arguments.of(createInvalidParams("anstn1993", "1111", "1111", "email@email.com", "김문수", "만수", "MALE")),
                Arguments.of(createInvalidParams("anstn1993", "11111111", "22222222", "email@email.com", "김문수", "만수", "MALE")),
                Arguments.of(createInvalidParams("anstn1993", "11111111", "11111111", "notemail", "김문수", "만수", "MALE")),
                Arguments.of(createInvalidParams("anstn1993", "11111111", "11111111", "email@email.com", "김ㅁㅅ", "만수", "MALE")),
                Arguments.of(createInvalidParams("anstn1993", "11111111", "11111111", "email@email.com", "김문수", "", "MALE"))
        );
    }

    private static MultiValueMap<String, String> createInvalidParams(String username,
                                                                    String password,
                                                                    String passwordCheck,
                                                                    String email,
                                                                    String name,
                                                                    String nickname,
                                                                    String sex) {
        MultiValueMap<String, String> invalidParams = new LinkedMultiValueMap<>();
        invalidParams.add("username", username);
        invalidParams.add("password", password);
        invalidParams.add("password-check", passwordCheck);
        invalidParams.add("email", email);
        invalidParams.add("name", name);
        invalidParams.add("nickname", nickname);
        invalidParams.add("sex", sex);
        return invalidParams;
    }

    private MockMultipartFile createMockMultipartFile() throws IOException {
        String imageFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        Resource imageResource = resourceLoader.getResource("classpath:image/" + imageFileName);
        return new MockMultipartFile("imageFile", imageResource.getFile().getName(), "image/jpg", imageResource.getInputStream());
    }
}