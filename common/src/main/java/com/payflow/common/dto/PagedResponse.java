package com.payflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Standard generic response wrapper for paginated and sorted query results.
 * Prevents N+1 query and memory exhaustion issues by enforcing structured pagination.
 *
 * @param <T> Data type of the items in the paginated list.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private int numberOfElements;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean empty;

    /**
     * Builds a {@link PagedResponse} from a Spring Data {@link Page}.
     *
     * @param page Spring Data Page object.
     * @param <T>  Content element type.
     * @return PagedResponse containing content and page metadata.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        if (page == null) {
            return empty();
        }
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Builds a {@link PagedResponse} by mapping entity items from a {@link Page} into DTOs.
     *
     * @param page   Source Page of entities.
     * @param mapper Function converting entity to target DTO.
     * @param <T>    Source type.
     * @param <R>    Target DTO type.
     * @return PagedResponse of mapped DTO items.
     */
    public static <T, R> PagedResponse<R> from(Page<T> page, Function<T, R> mapper) {
        if (page == null) {
            return empty();
        }
        return from(page.map(mapper));
    }

    /**
     * Alias for {@link #from(Page)}.
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return from(page);
    }

    /**
     * Alias for {@link #from(Page, Function)}.
     */
    public static <T, R> PagedResponse<R> of(Page<T> page, Function<T, R> mapper) {
        return from(page, mapper);
    }

    /**
     * Constructs a PagedResponse from raw content and pagination metadata.
     */
    public static <T> PagedResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        List<T> safeContent = (content != null) ? content : Collections.emptyList();
        int safePageSize = (pageSize > 0) ? pageSize : 1;
        int totalPages = (int) Math.ceil((double) totalElements / safePageSize);
        boolean isFirst = pageNumber == 0;
        boolean isLast = pageNumber >= totalPages - 1;

        return PagedResponse.<T>builder()
                .content(safeContent)
                .pageNumber(pageNumber)
                .pageSize(safePageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .numberOfElements(safeContent.size())
                .first(isFirst)
                .last(isLast)
                .hasNext(pageNumber < totalPages - 1)
                .hasPrevious(pageNumber > 0)
                .empty(safeContent.isEmpty())
                .build();
    }

    /**
     * Creates an empty PagedResponse.
     */
    public static <T> PagedResponse<T> empty() {
        return PagedResponse.<T>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(0)
                .totalElements(0L)
                .totalPages(0)
                .numberOfElements(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .empty(true)
                .build();
    }
}
