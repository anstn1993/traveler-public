package me.moonsoo.traveleroauthserver.config;

import me.moonsoo.commonmodule.account.AccountAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

//Authorization server 설정
@EnableAuthorizationServer
@Configuration
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountAuthService accountService;

    @Autowired
    TokenStore tokenStore;

    @Autowired
    DataSource dataSource;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder)
                //인증이 된 사용자(token이 발급된 사용자)만 oauth/check_token요청을 보낼 수 있다.
                .checkTokenAccess("isAuthenticated()")
        ;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /*
        Client정보를 db에 저장시키고 jdbc를 통해서 oauth_client_details테이블에서 client정보를 조회한다.
        조회는 ClientDetailsService의 구현체인 JdbcClientDetailsService가 담당
        */
        clients.jdbc(dataSource);
//        clients.jdbc(dataSource)
//                .withClient("traveler")
//                .secret(passwordEncoder.encode("pass"))
//                .refreshTokenValiditySeconds(60*60)
//                .accessTokenValiditySeconds(30*60)
//                .scopes("read", "write")
//                .authorizedGrantTypes("password", "refresh_token");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore)
                .userDetailsService(accountService)
                .authenticationManager(authenticationManager)
                .approvalStore(approvalStore())
        ;
    }

    @Bean
    public ApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource);
    }
}
