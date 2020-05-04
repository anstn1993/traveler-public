package me.moonsoo.traveleroauthserver.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.support.SqlLobValue;

import java.util.Optional;

public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, String> {
    Optional<OAuthAccessToken> findByToken(SqlLobValue token);
}
