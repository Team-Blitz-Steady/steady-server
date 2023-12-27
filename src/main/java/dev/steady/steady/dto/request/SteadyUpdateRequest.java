package dev.steady.steady.dto.request;

import dev.steady.steady.domain.SteadyMode;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SteadyUpdateRequest(
        @Size(min = 2, max = 35, message = "스테디 명은 2글자 이상 35글자 이하입니다.")
        String name,
        @Size(min = 1, max = 50, message = "스테디 소개는 1글자 이상 50글자 이하입니다.")
        String bio,
        @NotBlank(message = "연락 수단은 필수입니다.")
        String contact,
        @NotNull(message = "NULL은 올 수 없습니다.")
        SteadyType type,
        @NotNull(message = "NULL은 올 수 없습니다.")
        SteadyStatus status,
        @Range(min = 2, max = 10, message = "스테디 정원은 2이상 10 이하입니다.")
        int participantLimit,
        @NotNull(message = "NULL은 올 수 없습니다.")
        SteadyMode steadyMode,
        @NotNull(message = "NULL은 올 수 없습니다.")
        String scheduledPeriod,
        @FutureOrPresent(message = "마감 종료일은 오늘 이후로 설정해야합니다.")
        LocalDate deadline,
        @Size(min = 2, max = 25, message = "모집글 제목은 2글자 이상 25글자 이하입니다.")
        String title,
        @Size(min = 1, max = 3000, message = "모집글 내용은 1글자 이상 3000글자 이하입니다.")
        String content,
        @NotNull(message = "NULL은 올 수 없습니다.")
        List<Long> positions,
        @NotNull(message = "NULL은 올 수 없습니다.")
        List<Long> stacks
) {

}
