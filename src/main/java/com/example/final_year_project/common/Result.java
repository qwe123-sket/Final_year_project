package com.example.final_year_project.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 统一的接口返回格式。
 * 前端统一用 code == 200 判断请求是否成功。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "Success", data);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageData<T> {
        private List<T> list;
        private long total;
        private int page;
        private int size;
        private int totalPages;

        public static <T> PageData<T> of(List<T> list, long total, int page, int size) {
            int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
            return new PageData<>(
                    list != null ? list : Collections.emptyList(),
                    total, page, size, pages);
        }
    }
}
