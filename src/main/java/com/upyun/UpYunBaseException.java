package com.upyun;

/**
 * User: zjzhai
 * Date: 3/31/14
 */
public class UpYunBaseException extends RuntimeException {
    public UpYunBaseException() {
        super();
    }

    public UpYunBaseException(String message) {
        super(message);
    }

    public UpYunBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpYunBaseException(Throwable cause) {
        super(cause);
    }

    protected UpYunBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
