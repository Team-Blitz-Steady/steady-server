package dev.steady.steady.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long numberOfElements,
        CursorResponse cursor
) {

    public static <T> PageResponse<T> of(List<T> list, CursorResponse cursorResponse) {
        return new PageResponse<>(list,
                list.size(),
                cursorResponse
        );
    }

}
