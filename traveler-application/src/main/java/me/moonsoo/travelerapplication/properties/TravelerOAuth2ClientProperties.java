package me.moonsoo.travelerapplication.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "traveler.oauth2.client")
@Getter
@Setter
public class TravelerOAuth2ClientProperties {

    private String accessTokenUri;

    private String clientId;

    private String clientSecret;

    private String grantType;
}
