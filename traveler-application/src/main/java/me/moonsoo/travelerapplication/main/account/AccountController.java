package me.moonsoo.travelerapplication.main.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Controller
public class AccountController {

    @Autowired
    private RestTemplate restTemplate;

//    @Autowired
//    private HttpSession session;
//
//    @Autowired
//    private ModelMapper modelMapper;

    @GetMapping("/login")
    public String getLoginPage(Model model, @ModelAttribute("message") String message, @SessionAttribute(required = false) SessionAccount account, @CookieValue(required = false) String username) {
        //이미 로그인 상태인 경우
        if (account != null) {
            return "index.html";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }

        //사용자 계정이 쿠키로 존재하는 경우
        if(username != null) {
            model.addAttribute("username", username);
        }
        return "account/login";
    }


    @GetMapping("/logout")
    public String getLogoutPage() {
        return "account/logout";
    }
}
