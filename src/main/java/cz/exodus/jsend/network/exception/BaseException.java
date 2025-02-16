package cz.exodus.jsend.network.exception;

import lombok.Generated;

public class BaseException extends Exception {


    private final transient String errorInstanceId;
    public BaseException(String message) {
        super(message);
        this.errorInstanceId = null;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorInstanceId = null;
    }

    public BaseException(String message, String errorInstanceId) {
        super(message);
        this.errorInstanceId = errorInstanceId;
    }

    public BaseException(String message, Throwable cause, String errorInstanceId) {
        super(message, cause);
        this.errorInstanceId = errorInstanceId;
    }

    @Generated
    public String errorInstanceId() {
        return this.errorInstanceId;
    }
}
