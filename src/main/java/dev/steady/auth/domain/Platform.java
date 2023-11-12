package dev.steady.auth.domain;

import dev.steady.auth.exception.OAuthPlatformException;

import static dev.steady.auth.exception.AuthErrorCode.NOT_SUPPORTED_PLATFORM;

public enum Platform {

    KAKAO;

    public static Platform from(String platformName) {
        try {
            return Platform.valueOf(platformName.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new OAuthPlatformException(NOT_SUPPORTED_PLATFORM);
        }
    }

}
