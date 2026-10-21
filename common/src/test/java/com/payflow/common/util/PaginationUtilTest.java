package com.payflow.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilTest {

    @Test
    @DisplayName("sanitizePageNumber: Should return 0 when negative page is passed")
    void sanitizePageNumber_Negative_ReturnsZero() {
        assertThat(PaginationUtil.sanitizePageNumber(-5)).isEqualTo(0);
        assertThat(PaginationUtil.sanitizePageNumber(0)).isEqualTo(0);
        assertThat(PaginationUtil.sanitizePageNumber(4)).isEqualTo(4);
    }

    @Test
    @DisplayName("sanitizePageSize: Should enforce min default 10 and max clamp 100")
    void sanitizePageSize_BoundsEnforcement() {
        assertThat(PaginationUtil.sanitizePageSize(0)).isEqualTo(10);
        assertThat(PaginationUtil.sanitizePageSize(-20)).isEqualTo(10);
        assertThat(PaginationUtil.sanitizePageSize(25)).isEqualTo(25);
        assertThat(PaginationUtil.sanitizePageSize(150)).isEqualTo(100);
    }

    @Test
    @DisplayName("createSort: Should create ASC and DESC sorts correctly")
    void createSort_Directions() {
        Sort ascSort = PaginationUtil.createSort("createdAt", "ASC");
        assertThat(ascSort.getOrderFor("createdAt")).isNotNull();
        assertThat(ascSort.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);

        Sort descSort = PaginationUtil.createSort("amount", "DESC");
        assertThat(descSort.getOrderFor("amount")).isNotNull();
        assertThat(descSort.getOrderFor("amount").getDirection()).isEqualTo(Sort.Direction.DESC);

        Sort unsorted = PaginationUtil.createSort("   ", "ASC");
        assertThat(unsorted.isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("createPageable: Should create valid Pageable with sanitized inputs")
    void createPageable_Valid() {
        Pageable pageable = PaginationUtil.createPageable(2, 20, "id", "ASC");
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);

        Pageable defaultPageable = PaginationUtil.defaultPageable();
        assertThat(defaultPageable.getPageNumber()).isEqualTo(0);
        assertThat(defaultPageable.getPageSize()).isEqualTo(10);
    }
}
