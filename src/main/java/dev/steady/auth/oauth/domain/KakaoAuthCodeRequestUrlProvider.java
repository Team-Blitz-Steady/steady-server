package dev.steady.auth.oauth.domain;

import dev.steady.auth.config.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoAuthCodeRequestUrlProvider implements AuthCodeRequestUrlProvider {

    private static final String AUTHORIZATION_URL = "https://kauth.kakao.com/oauth/authorize";
    private final KakaoOAuthProperties kakaoOAuthProperties;

    @Override
    public Platform platform() {
        return Platform.KAKAO;
    }

    @Override
    public String provideUrl() {
        return UriComponentsBuilder
                .fromUriString(AUTHORIZATION_URL)
                .queryParam("client_id", kakaoOAuthProperties.getClientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                .queryParam("response_type", kakaoOAuthProperties.getResponseType())
                .toUriString();
    }

}
