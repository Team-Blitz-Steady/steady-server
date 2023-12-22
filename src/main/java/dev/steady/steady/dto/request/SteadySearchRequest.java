package dev.steady.steady.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record SteadySearchRequest(
        String steadyType,
        Integer page,
        String direction,
        String criteria,
        String steadyMode,
        String stack,
        String position,
        String status,
        String like,
        String keyword
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final String SORTING_CRITERIA = "promoted_at";

    public Pageable toPageable() {
        return PageRequest.of(
                page == null ? DEFAULT_PAGE : page,
                DEFAULT_SIZE,
                Sort.by(
                        direction == null ? Sort.Direction.DESC : Sort.Direction.fromString(direction),
                        criteria == null ? SORTING_CRITERIA : criteria
                )
        );
    }

}
