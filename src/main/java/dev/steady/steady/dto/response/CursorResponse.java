package dev.steady.steady.dto.response;

public record CursorResponse<T>(
    T prevCursor,
    T nextCursor
) {
}
