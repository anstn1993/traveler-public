package me.moonsoo.travelerrestapi.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


//traveler application과 연동 테스트시 필요한 mock smtp서버 설정
@Configuration
@Profile("test")
public class MockSmtpConfig {

    @Autowired
    private GreenMail greenMail;

    @Bean
    public GreenMail greenMail() {
        return new GreenMail(ServerSetup.SMTP);
    }

    @PostConstruct
    public void startSmtpServer() {
        greenMail.start();
        //smtp서버 유저 set
        greenMail.setUser("mansoo@localhost.com", "1111");
        greenMail.setUser("test@localhost.com", "1111");
    }

    @PreDestroy
    public void shutDownSmtpServer() {
        greenMail.stop();
    }

}
