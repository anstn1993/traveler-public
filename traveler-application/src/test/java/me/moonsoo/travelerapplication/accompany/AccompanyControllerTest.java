package me.moonsoo.travelerapplication.accompany;

import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerapplication.BaseControllerTest;
import me.moonsoo.travelerapplication.account.SessionAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AccompanyControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("동행 게시물 페이지 로드 테스트")
    public void getAccompanyBoardPage() throws Exception {
        mockMvc.perform(get("/accompanies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("/accompany/accompany-board"));
    }

    @Test
    @DisplayName("동행 게시물 업로드 페이지 로드 테스트")
    public void getUploadAccompanyPage() throws Exception {

        SessionAccount account = createSessionAccount();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("account", account);

        mockMvc.perform(get("/accompanies/upload")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("/accompany/upload-accompany"));
    }

    private SessionAccount createSessionAccount() {
        return SessionAccount.builder()
                .id(1)
                .email("user@email.com")
                .name("김유저")
                .roles(Set.of(AccountRole.USER))
                .sex(Sex.MALE)
                .nickname("user")
                .username("user")
                .build();
    }
}