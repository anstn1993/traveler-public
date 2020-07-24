package me.moonsoo.travelerapplication.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
