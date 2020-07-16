package me.moonsoo.travelerapplication.config;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.travelerapplication.main.account.SessionAccount;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//oauth2 인증에 성공했을 때 최종적으로 호출되는 핸들러로 로그인 성공 시에 쿠키 데이터 추가 및 세션 데이터를 추가해준다.
@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("authentication success");
        Account account = ((AccountAdapter) authentication.getPrincipal()).getAccount();
        String remember = request.getParameter("remember");//아이디 기억 체크박스 value
        if (remember != null) {
            Cookie cookie = new Cookie("username", account.getUsername());
            cookie.setPath("/login");
            cookie.setMaxAge(60 * 60 * 24 * 365);//1년동안 유지
            response.addCookie(cookie);//아이디 기억 value를 쿠키에 저장
        } else {
            //쿠기 중 username이 존재하면 그 쿠키 제거
            for (Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals("username")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        //세션에 사용자 정보 추가
        SessionAccount sessionAccount = modelMapper.map(account, SessionAccount.class);
        request.getSession().setAttribute("account", sessionAccount);
        response.sendRedirect("/");
    }
}
