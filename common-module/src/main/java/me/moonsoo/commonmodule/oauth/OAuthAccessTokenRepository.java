package me.moonsoo.commonmodule.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.support.SqlLobValue;

import java.sql.Blob;
import java.util.Optional;

public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, String> {
    Optional<OAuthAccessToken> findByToken(SqlLobValue token);
}
