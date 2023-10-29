package dev.steady.auth.oauth.dto.response;

import dev.steady.auth.domain.Account;
import dev.steady.auth.domain.Platform;

public interface OAuthUserInfoResponse {

    default Account toAccountEntity() {
        return Account.builder()
                .platform(this.getPlatform())
                .platformId(this.getPlatformId())
                .build();
    }

    Platform getPlatform();

    String getPlatformId();

}
