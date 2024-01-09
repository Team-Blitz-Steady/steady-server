package dev.steady.steady.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record PageResponse<T>(
        List<T> content,
        long numberOfElements,
        long page,
        String prevCursor,
        String nextCursor
) {

    public static <T> PageResponse<T> promotedAtResponse(List<T> list, long page, LocalDateTime prevCursor, LocalDateTime nextCursor) {
        if (Objects.isNull(prevCursor)) {
            return new PageResponse<>(
                    list,
                    list.size(),
                    page,
                    null,
                    String.valueOf(nextCursor)
            );
        }
        return new PageResponse<>(
                list,
                list.size(),
                page,
                String.valueOf(prevCursor),
                String.valueOf(nextCursor)
        );

    }

    public static <T> PageResponse<T> deadlineResponse(List<T> list, long page, LocalDate prevCursor, LocalDate nextCursor) {
        if (Objects.isNull(prevCursor)) {
            return new PageResponse<>(
                    list,
                    list.size(),
                    page,
                    null,
                    String.valueOf(nextCursor)
            );
        }
        return new PageResponse<>(
                list,
                list.size(),
                page,
                String.valueOf(prevCursor),
                String.valueOf(nextCursor)
        );
    }

}
