package com.example.final_year_project.exception;

/**
 * 自定义业务异常，会被 GlobalExceptionHandler 统一处理。
 * 前端会收到 {code, message} 格式的响应。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}
