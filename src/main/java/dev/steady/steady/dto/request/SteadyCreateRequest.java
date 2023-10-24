package dev.steady.steady.dto.request;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.steady.domain.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SteadyCreateRequest(
        String name,
        SteadyType type,
        int recruit_count,
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
                .recruit_count(recruit_count)
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
