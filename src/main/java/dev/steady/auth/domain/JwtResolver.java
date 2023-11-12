package dev.steady.auth.domain;

import dev.steady.auth.config.JwtProperties;
import dev.steady.global.auth.Authentication;
import dev.steady.global.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static dev.steady.auth.exception.AuthErrorCode.TOKEN_EMPTY;
import static dev.steady.auth.exception.AuthErrorCode.TOKEN_EXPIRED;
import static dev.steady.auth.exception.AuthErrorCode.TOKEN_INVALID;

@Component
public class JwtResolver {

    private static final String BEARER_TYPE = "Bearer ";
    private final SecretKey key;

    public JwtResolver(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    public Authentication getAuthentication(String bearerAccessToken) {
        String accessToken = removeBearerType(bearerAccessToken);
        Claims claims = parseClaims(accessToken);
        return Authentication.from(claims);
    }

    public void validateToken(String bearerToken) {
        String token = removeBearerType(bearerToken);
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationException(TOKEN_EMPTY);
        } catch (ExpiredJwtException exception) {
            throw new AuthenticationException(TOKEN_EXPIRED);
        } catch (MalformedJwtException | SignatureException exception) {
            throw new AuthenticationException(TOKEN_INVALID);
        }
    }

    public Claims parseClaims(String accessToken) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    private String removeBearerType(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_TYPE)) {
            return token.substring(BEARER_TYPE.length());
        }
        return null;
    }

}
