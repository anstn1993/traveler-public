package me.moonsoo.travelerrestapi.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "traveler-rest-api")
@Getter @Setter
public class AppProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String profileUri;

    private String createAccompanyAnchor;

    private String getAccompaniesAnchor;

    private String getAccompanyAnchor;

    private String updateAccompanyAnchor;

    private String createAccompanyCommentAnchor;

    private String getAccompanyCommentsAnchor;

    private String getAccompanyCommentAnchor;

    private String updateAccompanyCommentAnchor;

    private String createAccompanyChildCommentAnchor;

}
