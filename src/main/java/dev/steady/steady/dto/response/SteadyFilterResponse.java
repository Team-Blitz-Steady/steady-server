package dev.steady.steady.dto.response;

import dev.steady.steady.domain.Steady;

import java.util.List;

public record SteadyFilterResponse (
        List<Steady> result,
        Steady prevCursor
) {
}
