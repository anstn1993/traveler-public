package me.moonsoo.travelerapplication.main;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerapplication.BaseControllerTest;
import me.moonsoo.travelerapplication.account.SessionAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MainControllerTest extends BaseControllerTest {



    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("미인증 상태에서 메인 페이지 테스트")
    public void getIndex_annonymous() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"))
                .andExpect(model().attributeDoesNotExist("account"))
                .andExpect(content().string(containsString("로그인")))
                .andExpect(content().string(containsString("회원가입")))
        ;
    }

    @Test
    @DisplayName("인증 상태에서 메인 페이지 테스트")
    public void getIndex_authenticated() throws Exception {
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "11111111";
        Account account = createAccount(username, email, password, 0);
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);

        //세션에 사용자 정보를 넣어준다.
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("account", sessionAccount);

        mockMvc.perform(get("/")
                .session(mockHttpSession))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"))
                .andExpect(content().string(containsString("로그아웃")))
        ;
    }
}