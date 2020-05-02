package me.moonsoo.commonmodule.oauth;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "oauth_refresh_token")
public class OAuthRefreshToken {
    @Id
    private String tokenId;

    @Type(type = "blob")
    private String token;

    @Type(type = "blob")
    private String authentication;
}
