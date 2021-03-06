package me.moonsoo.travelerapplication.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "traveler-application")
@Getter @Setter
public class AppProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String restApiUrl;

    @NotEmpty
    private String createAccountUri;

    @NotEmpty
    private String deleteAccountUri;

}
