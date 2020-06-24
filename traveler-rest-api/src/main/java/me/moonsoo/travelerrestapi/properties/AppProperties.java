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

    private String getAccompanyChildCommentsAnchor;

    private String getAccompanyChildCommentAnchor;

    private String updateAccompanyChildCommentAnchor;

    private String createFollow;

    private String getAccountFollowingsAnchor;

    private String getAccountFollowingAnchor;

    private String getAccountFollowersAnchor;

    private String getAccountFollowerAnchor;

    private String createScheduleAnchor;

    private String getSchedulesAnchor;

    private String getScheduleAnchor;

    private String updateScheduleAnchor;

    private String createPostAnchor;

    private String getPostsAnchor;

    private String getPostAnchor;

    private String updatePostAnchor;

    private String createAccountAnchor;

    private String getAccountsAnchor;

    private String getAccountAnchor;

    private String updateAccountAnchor;

    private String createLikeAnchor;

    private String getLikesAnchor;

}
