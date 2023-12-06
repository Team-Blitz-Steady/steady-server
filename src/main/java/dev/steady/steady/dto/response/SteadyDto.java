package dev.steady.steady.dto.response;

import java.time.LocalDateTime;

public record SteadyDto(
        Long id,
        String name,
        LocalDateTime promoted_at
) {
}
