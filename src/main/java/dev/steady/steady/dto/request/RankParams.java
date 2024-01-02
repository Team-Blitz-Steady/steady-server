package dev.steady.steady.dto.request;

import dev.steady.steady.dto.RankCondition;
import jakarta.validation.constraints.Max;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record RankParams(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @Max(value = 10, message = "인기글은 최대 10개까지 조회가 가능합니다.")
        int limit,
        String type
) {

    public RankCondition toCondition() {
        return RankCondition.of(date, limit, type);
    }

}
