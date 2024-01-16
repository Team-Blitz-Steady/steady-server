package dev.steady.steady.uitl;

import dev.steady.steady.domain.Steady;
import io.jsonwebtoken.lang.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class Cursor {

    private LocalDateTime promotedAt;
    private LocalDate deadline;

    public static Cursor promotedAtCursorFrom(String cursor) {
        if (Strings.hasText(cursor)) {
            return new Cursor(CursorFormatter.getLocalDateTime(cursor), null);
        }
        return new Cursor(LocalDateTime.now(), null);
    }

    public static Cursor deadlineCursorFrom(String cursor) {
        if (Strings.hasText(cursor)) {
            return new Cursor(null, CursorFormatter.getLocalDate(cursor));
        }
        return new Cursor(null, LocalDate.now());
    }

    public static Cursor cursorFromSteady(Steady prevCursor) {
        if (Objects.isNull(prevCursor)) {
            return new Cursor(null, null);
        }
        return new Cursor(prevCursor.getPromotedAt(), prevCursor.getDeadline());
    }

    public boolean isPromotedAtCursor() {
        return Objects.nonNull(promotedAt);
    }

}
