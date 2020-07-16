package me.moonsoo.travelerapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//HttpSession을 spring session으로 대체하는 역할을 하는 SpringSessionRepositoryFilter를 생성해준다.
@EnableRedisHttpSession
@Profile("!test")
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
