package dev.steady.global.auth;

public class AuthFixture {

    public static AuthContext createAuthContext(Long userId) {
        var authContext = new AuthContext();
        authContext.setUserId(userId);
        return authContext;
    }

}
