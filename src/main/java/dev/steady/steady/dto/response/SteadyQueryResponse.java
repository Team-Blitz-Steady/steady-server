package dev.steady.steady.dto.response;

import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;
import dev.steady.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SteadyQueryResponse(
        Long id,
        String nickname,
        String profileImage,
        String title,
        SteadyType type,
        SteadyStatus status,
        LocalDate deadline,
        LocalDateTime createdAt,
        LocalDateTime promotedAt,
        int participantLimit,
        int numberOfParticipants,
        int viewCount,
        int likeCount,
        List<SteadyStackResponse> stacks
        // TODO: 2023-10-25  해쉬태그, 댓글 수
) {

    public static SteadyQueryResponse from(Steady steady) {
        User leader = steady.getParticipants().getLeader();
        List<SteadyStackResponse> stacks = steady.getSteadyStacks().stream()
                .map(SteadyStackResponse::from)
                .toList();
        return new SteadyQueryResponse(steady.getId(),
                leader.getNickname(),
                leader.getProfileImage(),
                steady.getTitle(),
                steady.getType(),
                steady.getStatus(),
                steady.getDeadline(),
                steady.getCreatedAt(),
                steady.getPromotion().getPromotedAt(),
                steady.getParticipantLimit(),
                steady.getNumberOfParticipants(),
                steady.getViewCount(),
                steady.getLikeCount(),
                stacks);
    }

}
