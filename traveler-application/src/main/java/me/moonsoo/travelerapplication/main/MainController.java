package me.moonsoo.travelerapplication.main;

import me.moonsoo.travelerapplication.main.account.SessionAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class MainController {

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    //메인 페이지 로드
    @GetMapping("/")
    public String getMain() {
        return "index.html";
    }
}
