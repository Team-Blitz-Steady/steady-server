package dev.steady.steady.dto.response;

import dev.steady.steady.domain.Steady;

import java.util.List;

public record SteadyFilterResponse(
        List<Steady> result,
        Steady prevCursorSteady,
        Steady nextCursorSteady
) {

    public SteadyFilterResponse(List<Steady> result, Steady prevCursorSteady, Steady nextCursorSteady) {
        this.result = result;
        this.prevCursorSteady = prevCursorSteady;
        if (result.isEmpty()) {
            this.nextCursorSteady = null;
        }else {
            this.nextCursorSteady = result.get(result.size() - 1);
        }
    }

    public SteadyFilterResponse(List<Steady> result, Steady prevCursor) {
        this(result, prevCursor, null);
    }

}
