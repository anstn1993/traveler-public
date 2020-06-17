package me.moonsoo.travelerrestapi.email;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;

@Service
public class EmailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    AppProperties appProperties;

    public void sendAuthMessage(Account account) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        String subject = "traveler 이용을 위한 인증 메일 입니다.";
        String text = "다음 링크에 접속하여 이메일 인증을 해주세요."
                + "<a href ='" + appProperties.getBaseUrl() + "/accounts/" + account.getId() +"/authenticateEmail?code=" + account.getAuthCode() + "'> 이메일 인증하기 </a>";;
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
        message.setSubject(subject);
        message.setContent(text, "text/html;charset=UTF8");
        javaMailSender.send(message);
    }

}
