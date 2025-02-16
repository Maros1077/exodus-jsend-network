package cz.exodus.jsend.network.exception;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(force = true)
public class ErrorDetails {
    private final String message;
    private final int code;
    private String errorInstanceId;
    private String component;

    public ErrorDetails(String message, int code, String errorInstanceId) {
        this.message = message;
        this.code = code;
        this.errorInstanceId = errorInstanceId;
    }

    public ErrorDetails(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public ErrorDetails(String message, int code, String errorInstanceId, String component) {
        this.message = message;
        this.code = code;
        this.errorInstanceId = errorInstanceId;
        this.component = component;
    }

    @Override
    public String toString() {
        return "ErrorDetails{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", errorInstanceId='" + errorInstanceId + '\'' +
                ", component='" + component + '\'' +
                '}';
    }
}
