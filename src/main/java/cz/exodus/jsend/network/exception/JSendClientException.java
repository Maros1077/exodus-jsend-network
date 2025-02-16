package cz.exodus.jsend.network.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class JSendClientException extends Exception {

    private final ErrorType errorType;
    private final HttpStatus httpStatus;
    private final ErrorDetails errorDetails;
}
