package me.moonsoo.travelerapplication.config;

import me.moonsoo.travelerapplication.properties.TravelerOAuth2ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;

@EnableWebSecurity
@Configuration
@EnableOAuth2Client
@Profile("test")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Qualifier("oauth2ClientContext")
    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    @Autowired
    private TravelerOAuth2ClientProperties clientProperties;//oauth2 client details property object

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public OAuth2RestTemplate oAuth2RestTemplate() {
        return new OAuth2RestTemplate(travelerClient(), oAuth2ClientContext);
    }

    @Bean
    public InMemoryTokenStore inMemoryTokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    public ResourceOwnerPasswordResourceDetails travelerClient() {
        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setAccessTokenUri(clientProperties.getAccessTokenUri());
        resourceDetails.setClientId(clientProperties.getClientId());
        resourceDetails.setClientSecret(clientProperties.getClientSecret());
        resourceDetails.setGrantType(clientProperties.getGrantType());
        resourceDetails.setAuthenticationScheme(AuthenticationScheme.form);
        resourceDetails.setClientAuthenticationScheme(AuthenticationScheme.header);
        return resourceDetails;
    }


    private Filter oAuth2ClientFilter() throws Exception {
        OAuth2ClientAuthenticationProcessingFilter oauth2ClientFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/traveler");
        oauth2ClientFilter.setAuthenticationFailureHandler(failureHandler());
        oauth2ClientFilter.setAuthenticationSuccessHandler(successHandler);
        oauth2ClientFilter.setRestTemplate(oAuth2RestTemplate());
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(inMemoryTokenStore());
        oauth2ClientFilter.setTokenServices(tokenServices);
        oauth2ClientFilter.setAuthenticationManager(authenticationManagerBean());
        oauth2ClientFilter.setApplicationEventPublisher(eventPublisher);
        return oauth2ClientFilter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager();
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(inMemoryTokenStore());
        authenticationManager.setTokenServices(tokenServices);
        return authenticationManager;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //static리소스 ignore
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/**").permitAll()
                .mvcMatchers(HttpMethod.POST, "/login/traveler", "/find-username/**", "/find-password/**", "/authenticate", "/invalidAuthCode", "/sign-up").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().ignoringAntMatchers("/invalidAuthCode",
                "/authenticate",
                "/sign-up",
                "/users/*/profile",
                "/users/*/password",
                "/users/*/withdrawl",
                "/users/followings/**",
                "/accompanies")
        ;
        http.formLogin().loginPage("/login").successForwardUrl("/").permitAll();
        http.logout().logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true);
        http.addFilterBefore(oAuth2ClientFilter(), BasicAuthenticationFilter.class);

        //세션 설정
        http.sessionManagement().maximumSessions(1).expiredUrl("/login");
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

}
