package me.moonsoo.travelerrestapi.config;

import me.moonsoo.commonmodule.config.SessionConfig;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

//SpringSessionRepositoryFilter가 등록되어어서 모든 request에 이 필터가 사용되게끔 해주는 initializer다.
public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {
    public SessionInitializer() {
        super(ResourceServerConfig.class, SecurityConfig.class, SessionConfig.class);
    }
}
