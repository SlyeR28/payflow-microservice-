package com.payflow.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class to build sanitized and safe {@link Pageable} requests across all services.
 * Caps maximum page size to prevent denial of service and memory overflow caused by
 * unbounded queries and cascading N+1 fetches.
 */
public final class PaginationUtil {

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    private PaginationUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Creates a {@link Pageable} with sanitized page index, capped page size, and optional sorting.
     *
     * @param page    0-based page index. Defaults to 0 if negative.
     * @param size    Number of records per page. Clamped between 1 and {@value #MAX_PAGE_SIZE}.
     * @param sortBy  Property name to sort by (e.g., "createdAt", "id"). If null or blank, unsorted is used.
     * @param sortDir Sort direction ("ASC" or "DESC", case-insensitive). Defaults to DESC.
     * @return Sanitized {@link Pageable} instance.
     */
    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int safePage = sanitizePageNumber(page);
        int safeSize = sanitizePageSize(size);

        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(safePage, safeSize);
        }

        Sort sort = createSort(sortBy, sortDir);
        return PageRequest.of(safePage, safeSize, sort);
    }

    /**
     * Creates a {@link Pageable} with page index, capped page size, and an explicit {@link Sort}.
     *
     * @param page 0-based page index.
     * @param size Number of records per page.
     * @param sort Sort specification.
     * @return Sanitized {@link Pageable} instance.
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        int safePage = sanitizePageNumber(page);
        int safeSize = sanitizePageSize(size);
        return (sort != null)
                ? PageRequest.of(safePage, safeSize, sort)
                : PageRequest.of(safePage, safeSize);
    }

    /**
     * Creates an unsorted {@link Pageable} with default or clamped pagination parameters.
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, null, null);
    }

    /**
     * Returns a default {@link Pageable} (page 0, size 10).
     */
    public static Pageable defaultPageable() {
        return PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    /**
     * Creates a {@link Sort} instance from property name and direction string.
     */
    public static Sort createSort(String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.unsorted();
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, sortBy.trim());
    }

    public static int sanitizePageNumber(int page) {
        return Math.max(page, DEFAULT_PAGE_NUMBER);
    }

    public static int sanitizePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
