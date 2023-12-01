package dev.steady.user.controller;

import dev.steady.auth.domain.Platform;
import dev.steady.auth.oauth.service.OAuthService;
import dev.steady.auth.service.AccountService;
import dev.steady.global.auth.Auth;
import dev.steady.global.auth.UserInfo;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.request.UserUpdateRequest;
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserNicknameExistResponse;
import dev.steady.user.dto.response.UserOtherDetailResponse;
import dev.steady.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;
    private final OAuthService oAuthService;

    @PostMapping("/profile")
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        Platform platform = accountService.registerUser(request.accountId(), userId);
        URI authCodeRequestUrl = oAuthService.getAuthCodeRequestUrlProvider(platform);

        return ResponseEntity.created(authCodeRequestUrl).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserMyDetailResponse> getMyUserDetail(@Auth UserInfo userInfo) {
        UserMyDetailResponse response = userService.getMyUserDetail(userInfo);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateUser(@RequestBody UserUpdateRequest request,
                                           @Auth UserInfo userInfo) {
        userService.updateUser(request, userInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/exist")
    public ResponseEntity<UserNicknameExistResponse> existsByNickname(@RequestParam String nickname) {
        UserNicknameExistResponse response = userService.existsByNickname(nickname);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserOtherDetailResponse> getOtherUserDetail(@PathVariable Long userId) {
        UserOtherDetailResponse response = userService.getOtherUserDetail(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> withdrawUser(@Auth UserInfo userInfo) {
        userService.withdrawUser(userInfo);
        return ResponseEntity.noContent().build();
    }

}
