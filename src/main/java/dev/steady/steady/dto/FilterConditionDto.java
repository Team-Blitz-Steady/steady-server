package dev.steady.steady.dto;

import dev.steady.steady.domain.SteadyMode;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;
import dev.steady.steady.dto.request.SteadySearchRequest;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record FilterConditionDto(
        SteadyType steadyType,
        SteadyMode steadyMode,
        List<String> stacks,
        List<String> positions,
        SteadyStatus status,
        boolean like,
        String keyword
) {

    public static FilterConditionDto from(SteadySearchRequest request) {
        SteadyType steadyType = filterSteadyType(request.steadyType());
        SteadyMode steadyMode = filterSteadyModeCondition(request.steadyMode());
        List<String> stack = filterStackOrPositionCondition(request.stack());
        List<String> position = filterStackOrPositionCondition(request.position());
        SteadyStatus status = filterSteadyStatusCondition(request.status());
        boolean like = filterLikeCondition(request.like());

        return new FilterConditionDto(steadyType,
                steadyMode,
                stack,
                position,
                status,
                like,
                request.keyword());
    }

    private static SteadyType filterSteadyType(String steadyType) {
        SteadyType result = null;
        if (StringUtils.hasText(steadyType)) {
            result = SteadyType.from(steadyType);
        }
        return result;
    }

    private static SteadyMode filterSteadyModeCondition(String steadyMode) {
        SteadyMode result = null;
        if (StringUtils.hasText(steadyMode)) {
            result = SteadyMode.from(steadyMode);
        }
        return result;
    }

    private static List<String> filterStackOrPositionCondition(String request) {
        List<String> result = new ArrayList<>();
        if (StringUtils.hasText(request)) {
            result = Arrays.stream(request.split(",")).toList();
        }
        return result;
    }

    private static SteadyStatus filterSteadyStatusCondition(String steadyStatus) {
        SteadyStatus result = null;
        if (StringUtils.hasText(steadyStatus)) {
            result = SteadyStatus.from(steadyStatus);
        }
        return result;
    }

    private static boolean filterLikeCondition(String like) {
        return like.equals("true");
    }

}


