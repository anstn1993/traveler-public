package me.moonsoo.travelerapplication.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerapplication.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AccountController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/login")
    public String getLoginPage(Model model, @ModelAttribute("message") String message, @SessionAttribute(required = false) SessionAccount account, @CookieValue(required = false) String username) {
        //이미 로그인 상태인 경우
        if (account != null) {
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }

        //사용자 계정이 쿠키로 존재하는 경우
        if (username != null) {
            model.addAttribute("username", username);
        }
        return "account/login";
    }


    @GetMapping("/logout")
    public String getLogoutPage() {
        return "account/logout";
    }

    @GetMapping("/find-username")
    public String getFindUsernamePage(@SessionAttribute(required = false) SessionAccount account,
                                      Model model,
                                      @ModelAttribute("message") String message) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "account/find-username";
    }


    //사용자 이름과 이메일 주소를 받아서 존재하는 사용자인지 검사하고
    //존재하는 사용자인 경우 인증 코드를 생성하여 사용자 메일로 전송하고 인증코드와 사용자 아이디를 세션에 저장하는 핸들러
    @PostMapping("/find-username")
    public String createAuthCodeAndSendMailForUsername(@SessionAttribute(required = false) SessionAccount account,
                                                       @RequestParam String name,
                                                       @RequestParam String email,
                                                       RedirectAttributes redirectAttributes,
                                                       HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }
        Optional<Account> targetAccountOpt = accountService.findUserByNameAndEmail(name, email);//사용자로부터 받은 값으로 조회한 사용자
        if (targetAccountOpt.isEmpty()) {//사용자 fetch가 되지 않은 경우
            redirectAttributes.addAttribute("message", "이름과 이메일을 다시 확인해주세요.");
            return "redirect:/find-username";
        }

        try {
            String authCode = emailService.sendAuthMessage(targetAccountOpt.get());//인증 메일 전송
            //사용자가 입력한 인증 번호와 비교하기 위해서 세션에 인증 코드를 저장한다.
            session.setAttribute("authCode", authCode);
            //인증 타입을 설정하여 인증 후의 로직을 처리
            session.setAttribute("authType", "username");
            //사용자가 인증에 성공했을 시 사용자 아이디를 반환해주기 위해서 username을 세션에 미리 저장해둔다.
            session.setAttribute("username", targetAccountOpt.get().getUsername());
            //인증 요청 페이지에서
            return "redirect:/authenticate";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/find-username";
        }
    }

    @GetMapping("/find-password")
    public String getFindPasswordPage(@SessionAttribute(required = false) SessionAccount account,
                                      Model model,
                                      @ModelAttribute("message") String message) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "account/find-password";
    }

    //사용자 아이디와 이메일 주소를 받아서 존재하는 사용자인지 검사하고
    //존재하는 사용자인 경우 인증 코드를 생성하여 사용자 메일로 전송하고 세션에 인증코드를 저장하는 핸들러
    @PostMapping("/find-password")
    public String createAuthCodeAndSendMailForPassword(@SessionAttribute(required = false) SessionAccount account,
                                                       @RequestParam String username,
                                                       @RequestParam String email,
                                                       RedirectAttributes redirectAttributes,
                                                       HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }
        Optional<Account> targetAccountOpt = accountService.findUsernameAndEmail(username, email);//사용자로부터 받은 값으로 조회한 사용자
        if (targetAccountOpt.isEmpty()) {//사용자 fetch가 되지 않은 경우
            redirectAttributes.addAttribute("message", "아이디와 이메일을 다시 확인해주세요.");
            return "redirect:/find-password";
        }

        try {
            String authCode = emailService.sendAuthMessage(targetAccountOpt.get());//인증 메일 전송
            //사용자가 입력한 인증 번호와 비교하기 위해서 세션에 인증 코드를 저장한다.
            session.setAttribute("authCode", authCode);
            //인증 타입을 설정하여 인증 후의 로직을 처리
            session.setAttribute("authType", "password");
            session.setAttribute("username", targetAccountOpt.get().getUsername());
            //인증 요청 페이지에서
            return "redirect:/authenticate";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/find-password";
        }
    }

    @GetMapping("/authenticate")
    public String getAuthenticatePage(@SessionAttribute(required = false) SessionAccount account,
                                      @SessionAttribute(required = false) String authCode,
                                      HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (authCode == null) {//세션에 인증 코드가 없는 상태에서 요청한 경우
            return "redirect:/";
        }
        return "account/authenticate";
    }

    //param: code-사용자가 입력한 코드, authCode-세션에 저장되어 있는 인증 코드
    //사용자가 입력한 인증코드를 받아서 세션에 저장된 인증 코드와 비교하여 일치, 불일치 여부에 따라 다른 응답을 하는 핸들러
    @PostMapping("/authenticate")
    public ResponseEntity authenticateUserForUsername(@SessionAttribute(required = false) String authCode,
                                                      @RequestParam("authCode") String code,
                                                      HttpSession session) {
        if (!authCode.equals(code)) {//사용자가 입력한 코드와 인증코드가 일치하지 않는 경우
            return ResponseEntity.badRequest().build();
        } else {//사용자가 입력한 코드와 인증코드가 일치하는 경우
            //인증 타입을 응답 본문에 추가한다.
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("authType", (String) session.getAttribute("authType"));
            return ResponseEntity.ok(objectNode);
        }
    }

    @GetMapping("/find-username/result")
    public String returnUsername(@SessionAttribute(required = false) SessionAccount account,
                                 @SessionAttribute(required = false) String username,
                                 Model model,
                                 HttpSession session) {
        if (account != null) {//이미 로그인 상태인 경우
            return "redirect:/";
        }

        //인증 코드가 세션에 존재한다는 건 아직 인증을 하지 못 했다는 것을 의미하기 때문에
        //메인 페이지로 redirect
        if (username == null) {
            return "redirect:/";
        }
        model.addAttribute("username", username);
        session.invalidate();
        return "account/find-username-result";
    }

    @GetMapping("/find-password/result")
    public String returnPassword(@SessionAttribute(required = false) SessionAccount account,
                                 @SessionAttribute(required = false) String authCode,
                                 HttpSession session) {
        if (account != null) {//이미 로그인 상태인 경우
            return "redirect:/";
        }

        //인증 코드가 세션에 존재한다는 건 아직 인증을 하지 못 했다는 것을 의미하기 때문에
        //메인 페이지로 redirect
        if (authCode == null) {
            return "redirect:/";
        }
        session.removeAttribute("authType");
        session.removeAttribute("authCode");
        return "account/find-password-result";
    }

    @PostMapping("/find-password/result")
    public ResponseEntity setUpPassword(@RequestParam String password,
                                        @SessionAttribute(required = false) String username,
                                        HttpSession session) {
        if(username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        accountService.updatePassword(username, password);
        session.invalidate();
        return ResponseEntity.ok().build();
    }


    //사용자 인증 페이지에서 벗어나거나 새로고침을 하는 경우 인증 초기화 여부를 세션에서 삭제하기 위한 핸들러
    @PostMapping("/invalidAuthCode")
    public ResponseEntity invalidAuthCode(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

}
