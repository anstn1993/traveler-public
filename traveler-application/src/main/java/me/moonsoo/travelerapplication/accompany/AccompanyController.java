package me.moonsoo.travelerapplication.accompany;

import me.moonsoo.travelerapplication.account.SessionAccount;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/accompanies")
public class AccompanyController {

    //동행 구하기 게시판 페이지 요청 핸들러
    @GetMapping
    public String getAccompanyBoardPage() {
        return "/accompany/accompany-board";
    }

    //동행 게시물 업로드 페이지 요청 핸들러
    @GetMapping("/upload")
    public String getUploadAccompanyPage(@SessionAttribute(required = false)SessionAccount account) {
        //로그인 상태가 아니라면 login 페이지로 리다이렉트
        if(account == null) {
            return "redirect:/account/login";
        }
        return "/accompany/upload-accompany";
    }
}
