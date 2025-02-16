package cz.exodus.jsend.network.exception;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ErrorType {
    FAIL("fail"),
    ERROR("error");

    private final String jsendStatus;

    ErrorType(String jsendStatus) {
        this.jsendStatus = jsendStatus;
    }

    public static ErrorType fromJsendStatus(String jsendStatus) {
        for(ErrorType e: ErrorType.values()) {
            if(Objects.equals(e.jsendStatus, jsendStatus)) {
                return e;
            }
        }
        return null;
    }
}
