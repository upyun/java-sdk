package com.upyun;

/**
 * User: zjzhai
 * Date: 4/1/14
 */
public class UpYunServerErrorException extends UpYunBaseException {
    public UpYunServerErrorException() {
        super();
    }

    public UpYunServerErrorException(String message) {
        super(message);
    }

    public UpYunServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpYunServerErrorException(Throwable cause) {
        super(cause);
    }

    protected UpYunServerErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
