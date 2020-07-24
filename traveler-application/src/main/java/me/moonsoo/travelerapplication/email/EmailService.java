package me.moonsoo.travelerapplication.email;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public String sendAuthMessage(Account account) throws MessagingException {
        String authCode = createAuthCode();
        MimeMessage message = javaMailSender.createMimeMessage();
        String subject = "traveler 아이디/비밀번호 찾기를 위한 인증 메일 입니다.";
        String text = "인증번호: " + authCode;
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
        message.setSubject(subject);
        message.setContent(text, "text/html;charset=UTF8");
        javaMailSender.send(message);
        return authCode;
    }

    private String createAuthCode() {
        int length = 6;//인증 코드 길이
        StringBuffer sb = new StringBuffer(length);//6자리 숫자가 들어갈 문자열 버퍼 생성

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            //0~9사이의 난수
            String ranStr = Integer.toString(random.nextInt(10));
            //인증 코드 버퍼에 해당 난수가 존재하지 않는 경우
            if (!sb.toString().contains(ranStr)) {
                sb.append(ranStr);//버퍼에 난수 추가
            }
            //인증 코드 버퍼에 해당 난수가 존재하는 경우
            else {
                i--;
            }
        }
        return sb.toString();
    }
}
