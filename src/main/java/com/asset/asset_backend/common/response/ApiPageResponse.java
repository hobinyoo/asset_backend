package com.asset.asset_backend.common.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ApiPageResponse<T> {

    private final long totalElements;
    private final int totalPages;
    private final int size;
    private final int pageNumber;
    private final boolean last;
    private final List<T> content;

    public ApiPageResponse(Page<?> page, List<T> content) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.pageNumber = page.getNumber();
        this.last = page.isLast();
        this.content = content;
    }

    public static <T> ApiPageResponse<T> of(Page<?> page, List<T> content) {
        return new ApiPageResponse<>(page, content);
    }
}