package dev.steady.steady.uitl;

import io.jsonwebtoken.lang.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Cursor {

    private LocalDateTime promotedAt;
    private LocalDate deadline;

    public static Cursor of(String criteria, String cursor) {
        if (criteria.equals("deadline")) {
            if (Strings.hasText(cursor)) {
                return new Cursor(null, CursorFormatter.getLocalDate(cursor));
            }
            return new Cursor(null, LocalDate.now());
        }

        if (Strings.hasText(cursor)) {
            return new Cursor(CursorFormatter.getLocalDateTime(cursor), null);
        }
        return new Cursor(LocalDateTime.now(), null);
    }

}
