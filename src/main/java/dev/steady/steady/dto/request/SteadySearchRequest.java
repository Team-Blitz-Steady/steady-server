package dev.steady.steady.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record SteadySearchRequest(
        Integer page,
        String direction,
        String criteria,
        String cursor,
        String steadyType,
        String steadyMode,
        String stack,
        String position,
        String status,
        String like,
        String keyword
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;
    private static final String SORTING_CRITERIA = "promotion.promotedAt";

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
