package dev.steady.user.dto.response;

import dev.steady.auth.domain.Platform;
import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;

import java.util.List;

public record UserMyDetailResponse(
        Platform platform,
        Long userId,
        String profileImage,
        String nickname,
        String bio,
        PositionResponse position,
        List<StackResponse> stacks
) {

    public static UserMyDetailResponse from(Platform platform, User user, List<UserStack> userStacks) {
        PositionResponse position = PositionResponse.from(user.getPosition());
        List<StackResponse> stacks = userStacks.stream()
                .map(userStack -> StackResponse.from(userStack.getStack()))
                .toList();

        return new UserMyDetailResponse(
                platform,
                user.getId(),
                user.getProfileImage(),
                user.getNickname(),
                user.getBio(),
                position,
                stacks
        );
    }

}
