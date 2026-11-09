package com.example.scores.util;

/**
 * 输入流结束异常。
 * <p>
 * 当标准输入被关闭（例如通过管道传入脚本后读完、或用户按下 Ctrl+Z）时抛出，
 * 由 {@code MainApp} 统一捕获并退出程序，避免菜单在空输入上死循环。
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException(String message) {
        super(message);
    }
}
