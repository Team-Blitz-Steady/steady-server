package dev.steady.user.dto.request;

import java.util.List;

public record UserUpdateRequest(
        String profileImage,
        String nickname,
        String bio,
        Long positionId,
        List<Long> stackIds
) {
}
