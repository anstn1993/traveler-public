package me.moonsoo.travelerapplication.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;

@ControllerAdvice
public class ExceptionProcessor {

    @ExceptionHandler(PageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException() {
        return "error/404";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbiddenException() {
        return "error/403";
    }

    //access token이 만료되면 이 핸들러가 호출된다. session데이터를 모두 지워주고 401 status를 반환해준다.
    @ExceptionHandler(OAuth2AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity handlerOAuth2AccessDeniedException(HttpSession session) {
        session.invalidate();//세션 데이터 만료
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
