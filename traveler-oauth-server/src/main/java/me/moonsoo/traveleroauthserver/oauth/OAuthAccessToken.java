package me.moonsoo.traveleroauthserver.oauth;

import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "oauth_access_token")
@Getter
public class OAuthAccessToken {

    @Id
    @Column(unique = true, nullable = false)
    private String authenticationId;

    private String tokenId;

    @Type(type = "blob")
    private String[] token;

    @Column(name = "user_name")
    private String username;

    private String ClientId;

    @Type(type = "blob")
    private String[] authentication;

    private String refreshToken;

}
