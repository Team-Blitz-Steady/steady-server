package dev.steady.steady.dto.response;

import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;

import java.time.LocalDate;

public record SteadyRankResponse(
        Long steadyId,
        String title,
        SteadyType type,
        SteadyStatus status,
        LocalDate deadline,
        int participantLimit,
        int numberOfParticipants,
        int viewCount,
        int likeCount
) {

    public static SteadyRankResponse from(Steady steady) {
        return new SteadyRankResponse(
                steady.getId(),
                steady.getTitle(),
                steady.getType(),
                steady.getStatus(),
                steady.getDeadline(),
                steady.getParticipantLimit(),
                steady.getNumberOfParticipants(),
                steady.getViewCount(),
                steady.getLikeCount()
        );
    }

}
