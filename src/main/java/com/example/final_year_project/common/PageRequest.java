package com.example.final_year_project.common;

/**
 * 分页参数：统一最大每页条数，避免过大查询
 */
public final class PageRequest {

    public static final int MAX_SIZE = 50;
    public static final int DEFAULT_SIZE = 10;

    private PageRequest() {}

    public static int clampSize(int size) {
        if (size < 1) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }
}
