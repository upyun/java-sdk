package com.upyun;

/**
 * User: zjzhai
 * Date: 4/1/14
 */
public class UpYunNotFoundException extends UpYunBaseException {
    public UpYunNotFoundException() {
        super();
    }

    public UpYunNotFoundException(String message) {
        super(message);
    }

    public UpYunNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpYunNotFoundException(Throwable cause) {
        super(cause);
    }

    protected UpYunNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
