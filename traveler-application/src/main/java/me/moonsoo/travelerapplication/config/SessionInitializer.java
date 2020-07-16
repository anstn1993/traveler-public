package me.moonsoo.travelerapplication.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

//SpringSessionRepositoryFilter가 등록되어어서 모든 request에 이 필터가 사용되게끔 해주는 initializer다.
public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {
    public SessionInitializer() {
        super(SecurityConfig.class, SessionConfig.class);
    }
}
