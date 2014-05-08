package com.upyun;

/**
 * User: zjzhai
 * Date: 3/31/14
 */
public class UpYunIOException extends UpYunBaseException {
    public UpYunIOException() {
        super();
    }

    public UpYunIOException(String message) {
        super(message);
    }

    public UpYunIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpYunIOException(Throwable cause) {
        super(cause);
    }

    protected UpYunIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
