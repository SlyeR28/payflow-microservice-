package com.payflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.function.Function;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Retained for backward compatibility
    public static <T> ApiResponse<T> sucess(T data) {
        return success(data);
    }

    // Retained for backward compatibility
    public static <T> ApiResponse<T> sucess(String message, T data) {
        return success(message, data);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<PagedResponse<T>> paged(Page<T> page) {
        return ApiResponse.<PagedResponse<T>>builder()
                .success(true)
                .data(PagedResponse.from(page))
                .build();
    }

    public static <T> ApiResponse<PagedResponse<T>> paged(String message, Page<T> page) {
        return ApiResponse.<PagedResponse<T>>builder()
                .success(true)
                .message(message)
                .data(PagedResponse.from(page))
                .build();
    }

    public static <T, R> ApiResponse<PagedResponse<R>> paged(Page<T> page, Function<T, R> mapper) {
        return ApiResponse.<PagedResponse<R>>builder()
                .success(true)
                .data(PagedResponse.from(page, mapper))
                .build();
    }

    public static <T, R> ApiResponse<PagedResponse<R>> paged(String message, Page<T> page, Function<T, R> mapper) {
        return ApiResponse.<PagedResponse<R>>builder()
                .success(true)
                .message(message)
                .data(PagedResponse.from(page, mapper))
                .build();
    }

    public static <T> ApiResponse<PagedResponse<T>> paged(PagedResponse<T> pagedResponse) {
        return ApiResponse.<PagedResponse<T>>builder()
                .success(true)
                .data(pagedResponse)
                .build();
    }

    public static <T> ApiResponse<PagedResponse<T>> paged(String message, PagedResponse<T> pagedResponse) {
        return ApiResponse.<PagedResponse<T>>builder()
                .success(true)
                .message(message)
                .data(pagedResponse)
                .build();
    }
}
