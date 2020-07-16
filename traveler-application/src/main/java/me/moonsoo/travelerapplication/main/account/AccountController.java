package me.moonsoo.travelerapplication.main.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.commonmodule.account.CurrentAccount;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.filter.OAuth2AuthenticationFailureEvent;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class AccountController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpSession session;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/login")
    public String getLoginPage(Model model, @ModelAttribute("message") String message, @SessionAttribute SessionAccount account) {
        //이미 로그인 상태인 경우
        if (account != null) {
            return "index.html";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "account/login";
    }


    @GetMapping("/logout")
    public String getLogoutPage() {
        return "account/logout";
    }

    //인증이 성공적으로 이루어지면 콜백되는 이벤트 핸들러
    //로그인된 사용자 정보를 세션에 저장한다.
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        System.out.println("authentication success");
        Account account = ((AccountAdapter) event.getAuthentication().getPrincipal()).getAccount();
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);
        session.setAttribute("account", sessionAccount);
    }
}
