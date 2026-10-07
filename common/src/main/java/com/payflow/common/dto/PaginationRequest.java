package com.payflow.common.dto;

import com.payflow.common.util.PaginationUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

/**
 * Reusable request DTO for endpoints accepting pagination and sorting query parameters.
 * Can be used as a controller method parameter (Spring automatically binds query params)
 * or manually instantiated.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationRequest {

    @Min(value = 0, message = "Page index must be greater than or equal to 0")
    @Builder.Default
    private int page = PaginationUtil.DEFAULT_PAGE_NUMBER;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = PaginationUtil.MAX_PAGE_SIZE, message = "Page size cannot exceed " + PaginationUtil.MAX_PAGE_SIZE)
    @Builder.Default
    private int size = PaginationUtil.DEFAULT_PAGE_SIZE;

    private String sortBy;

    @Builder.Default
    private String sortDir = PaginationUtil.DEFAULT_SORT_DIRECTION;

    /**
     * Converts this request into a {@link Pageable}.
     */
    public Pageable toPageable() {
        return PaginationUtil.createPageable(page, size, sortBy, sortDir);
    }

    /**
     * Converts this request into a {@link Pageable} with custom fallback sorting.
     */
    public Pageable toPageable(String defaultSortBy, String defaultSortDir) {
        String effectiveSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : defaultSortBy;
        String effectiveSortDir = (sortDir != null && !sortDir.isBlank()) ? sortDir : defaultSortDir;
        return PaginationUtil.createPageable(page, size, effectiveSortBy, effectiveSortDir);
    }
}
