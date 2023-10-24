package dev.steady.steady.dto.request;

import dev.steady.steady.domain.*;
import dev.steady.steadyForm.domain.SteadyForm;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SteadyCreateRequest(
        String name,
        SteadyType type,
        int recruitCount,
        SteadyMode steadyMode,
        LocalDate openingDate,
        LocalDate deadline,
        String title,
        String content,
        Long steadyFormId
) {

    public Steady toEntity(Promotion promotion, SteadyForm steadyForm) {
        return Steady.builder()
                .name(name)
                .type(type)
                .status(SteadyStatus.RECRUITING)
                .recruitCount(recruitCount)
                .steadyMode(steadyMode)
                .openingDate(openingDate)
                .deadline(deadline)
                .title(title)
                .content(content)
                .promotion(promotion)
                .steadyForm(steadyForm)
                .build();
    }

}
