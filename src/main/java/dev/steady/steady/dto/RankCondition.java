package dev.steady.steady.dto;

import java.time.LocalDate;

public record RankCondition(
        LocalDate date,
        int limit,
        RankType type
) {

    public static RankCondition of(LocalDate date, int limit, String type) {
        return new RankCondition(date, limit, RankType.from(type));
    }

}
