package com.upyun;

/**
 * User: zjzhai
 * Date: 4/1/14
 */
public class UpYunAuthenticateException extends UpYunBaseException {
    public UpYunAuthenticateException() {
        super();
    }

    public UpYunAuthenticateException(String message) {
        super(message);
    }

    public UpYunAuthenticateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpYunAuthenticateException(Throwable cause) {
        super(cause);
    }

    protected UpYunAuthenticateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
