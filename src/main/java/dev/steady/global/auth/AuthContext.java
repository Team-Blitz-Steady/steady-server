package dev.steady.global.auth;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
public class AuthContext {

    private Long userId;

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
