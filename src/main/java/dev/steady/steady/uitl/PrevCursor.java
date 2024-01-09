package dev.steady.steady.uitl;

import dev.steady.steady.domain.Steady;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class PrevCursor {

    private LocalDateTime promotedAt;
    private LocalDate deadline;

    public static PrevCursor from(Steady prevCursor) {
        if (Objects.isNull(prevCursor)) {
            return new PrevCursor(null, null);
        }
        return new PrevCursor(prevCursor.getPromotedAt(), prevCursor.getDeadline());
    }

}
