package dev.steady.auth.fixture;

import dev.steady.auth.domain.Account;
import dev.steady.auth.domain.Platform;
import dev.steady.user.domain.User;

public class AccountFixture {

    public static Account createAccount() {
        return new Account(Platform.KAKAO, "111111");
    }

    public static Account createAccount(User user) {
        Account account = new Account(Platform.KAKAO, "111111");
        account.registerUser(user);
        return account;
    }
    
}
