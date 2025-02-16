package cz.exodus.jsend.network.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import cz.exodus.jsend.network.rest.JSendResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSendResponse> handleGenericException(Exception ex) {
        if (ex.getCause() instanceof JSendClientException) {
            JSendClientException clientException = (JSendClientException) ex.getCause();
            ErrorDetails errorDetails = clientException.getErrorDetails();
            return ResponseEntity.status(clientException.getHttpStatus()).body(new JSendResponse(clientException.getErrorType().getJsendStatus(), errorDetails));
        }
        ErrorDetails errorDetails = new ErrorDetails("An unexpected error occurred", 500);
        log.error("Generic exception: {}", ex.getMessage());
        JSendResponse response = new JSendResponse("error", errorDetails);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

