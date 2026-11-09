package com.example.scores.util;

/**
 * 业务异常：用于把“学号重复”“分数超范围”等可预期的错误
 * 从 DAO/Service 层传递到控制台界面，由界面统一打印提示。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
