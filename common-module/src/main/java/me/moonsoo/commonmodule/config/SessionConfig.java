package me.moonsoo.commonmodule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.concurrent.ConcurrentHashMap;

//HttpSession을 spring session으로 대체하는 역할을 하는 SpringSessionRepositoryFilter를 생성해준다.
@EnableRedisHttpSession
public class SessionConfig {

    /*
    스프링 세션을 redis 서버로 연결시켜주는 connection factory
    default port: 6379
    */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }
}
