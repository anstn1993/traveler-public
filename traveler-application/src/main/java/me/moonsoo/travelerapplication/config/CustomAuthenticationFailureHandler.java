package me.moonsoo.travelerapplication.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        FlashMap flashMap = new FlashMap();
        System.out.println("onAuthenticationFailure");
        Throwable cause = exception.getCause().getCause();
        if (cause instanceof ResourceAccessException) {
            System.out.println("oauth2 server is shut down");
            request.setAttribute("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해보세요.");
            flashMap.put("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해보세요.");
        } else if (cause instanceof InvalidGrantException) {
            System.out.println("invalid username and password");
            request.setAttribute("message", "아이디와 비밀번호를 확인하세요.");
            flashMap.put("message", "아이디와 비밀번호를 확인하세요.");
        } else if (cause instanceof HttpClientErrorException) {
            System.out.println("not authenticated email");
            request.setAttribute("message", "이메일 인증 후 로그인이 가능합니다.");
            flashMap.put("message", "이메일 인증 후 로그인이 가능합니다.");
        }
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect("/login");
    }
}
