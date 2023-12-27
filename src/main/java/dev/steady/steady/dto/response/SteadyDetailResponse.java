package dev.steady.steady.dto.response;

import dev.steady.steady.domain.ScheduledPeriod;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyMode;
import dev.steady.steady.domain.SteadyPosition;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SteadyDetailResponse(
        Long id,
        LeaderResponse leaderResponse,
        String name,
        String bio,
        String contact,
        SteadyType type,
        SteadyStatus status,
        int participantLimit,
        int numberOfParticipants,
        SteadyMode steadyMode,
        ScheduledPeriod scheduledPeriod,
        LocalDate deadline,
        String title,
        String content,
        int viewCount,
        List<SteadyPositionResponse> positions,
        List<SteadyStackResponse> stacks,
        boolean isLeader,
        Long applicationId,
        int promotionCount,
        LocalDateTime createdAt,
        LocalDate finishedAt,
        boolean isReviewEnabled,
        boolean isLiked
) {

    public static SteadyDetailResponse of(Steady steady,
                                          List<SteadyPosition> positions,
                                          boolean isLeader,
                                          Long applicationId,
                                          boolean isLiked) {
        return new SteadyDetailResponse(steady.getId(),
                LeaderResponse.from(steady.getLeader()),
                steady.getName(),
                steady.getBio(),
                steady.getContact(),
                steady.getType(),
                steady.getStatus(),
                steady.getParticipantLimit(),
                steady.getNumberOfParticipants(),
                steady.getSteadyMode(),
                steady.getScheduledPeriod(),
                steady.getDeadline(),
                steady.getTitle(),
                steady.getContent(),
                steady.getViewCount(),
                positions.stream()
                        .map(SteadyPositionResponse::from)
                        .toList(),
                steady.getSteadyStacks().stream()
                        .map(SteadyStackResponse::from)
                        .toList(),
                isLeader,
                applicationId,
                steady.getPromotionCount(),
                steady.getCreatedAt(),
                steady.getFinishedAt(),
                steady.isReviewEnabled(),
                isLiked
        );
    }

}
