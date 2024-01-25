package dev.steady.steady.uitl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorFormatter {

    private static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDateTime getLocalDateTime(String cursor) {
        return LocalDateTime.parse(cursor, localDateTimeFormatter);
    }

    public static LocalDate getLocalDate(String cursor) {
        return LocalDate.parse(cursor, localDateFormatter);
    }

}
