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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private EmailService emailService;

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @RegisterExtension
    public static SmtpServerExtension smtpServerExtension = new SmtpServerExtension(new GreenMail(ServerSetup.SMTP));

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
                .andExpect(view().name("redirect:/find-username/authenticate"))
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
        MvcResult authenticateResult = mockMvc.perform(post("/find-username/authenticate")
                .session(authCodeMockSession)
                .with(csrf())
                .param("authCode", authCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        HttpSession afterAuthSession = authenticateResult.getRequest().getSession();
        assertThat(afterAuthSession.getAttribute("authCode")).isNull();

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
        assertThat(findUsernameSession.getAttribute("username")).isNull();
    }

    @Test
    @DisplayName("비밀번호 찾기 본인 인증")
    public void authenticate_Find_Password() {

    }

}