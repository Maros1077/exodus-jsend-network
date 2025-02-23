# Network SDK

Implements [JSend](https://github.com/omniti-labs/jsend) specification for handling JSON responses and provides base
exception handling.

## Addeding dependency

The `jsend-library` is not available in any public Maven repository and must be added manually to your project. To
include it, you need to specify the dependency in your `pom.xml` with a system scope and provide the path to the `.jar`
file on your local filesystem.

```
<dependency>
    <groupId>cz.exodus</groupId>
    <artifactId>jsend-library</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/jsend-network-1.0-SNAPSHOT.jar</systemPath>
</dependency>
```

## Importing configuration

Add library classes to a `@SpringBootApplication`

```java
@SpringBootApplication()
@Import({
		WebClientConfig.class,
		GlobalExceptionHandler.class,
		JSendResponseAdvice.class
})
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}

```

### WebClientConfig

Sets up a pre-configured `WebClient.Builder`, ensuring consistent JSON processing and streamlined HTTP communication.

### BaseJSendClient

An abstract base class designed to simplify communication with APIs that follow the JSend response format. It offers
classic `Mono` object, but also custom `Result` for synchronized calls.

#### Error Handling:

- Detects non-200 HTTP status codes and processes them as errors (other successful HTTP status will come in the future).
- Extracts error details from JSend responses and wraps them in a custom exception (`JSendClientException`).
- Logs error details for debugging purposes.

#### Example usage

Example custom client:

```java
@Service
public class MyClient extends BaseJSendClient {

    private static final String SERVICE_NAME = "my-service";

    @Autowired
    public MyClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        super(webClientBuilder.baseUrl("http://localhost:8080").build(), SERVICE_NAME, objectMapper);
    }

    public Mono<LoginResponse> getLogin(LoginRequest request) {
        return executePostRequest("/test/v1/login", request, LoginResponse.class);
    }
    
    public Result<IssueResponse, JSendClientException> issueToken(IssueRequest request) {
        return executePostRequestSync("/test/v1/issue", request, IssueResponse.class);
    }
}

```

### Result

The `Result<S, F>` class is a generic wrapper for handling operations that can either succeed or fail. It follows the
`Result<Success, Failure>` pattern, commonly used for error handling without exceptions.

#### Example usage

Example failure handling:

```java
    Result<ValidateResponse, JSendClientException> tokenResponse = stsClient.validateToken(token);
    if (tokenResponse.isFailure()) {
        if (tokenResponse.getFailure().getHttpStatus().value() == 401 || tokenResponse.getFailure().getHttpStatus().value() == 403)
            throw new UnauthorizedException();
        else throw tokenResponse.getFailure();
    }
    String metadata = tokenResponse.getSuccess().getMetadata();
```

Example map:

```java
    AuthResponse authResponse = response.mapSuccess(issueResponse -> new AuthResponse(issueResponse.getToken(), issueResponse.getType(), issueResponse.getExpiresIn())).getSuccess();
```

### Base Exception

Foundation for creating custom exceptions. Introduces an additional field, `errorInstanceId`, which can be used to
uniquely identify specific instances of errors.

#### Example usage

Defining custom error:

```java
@Getter
@AllArgsConstructor
public enum MyError {

    REQUEST_INVALID(1000, FAIL, HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAILED(1001, FAIL, HttpStatus.UNAUTHORIZED);

    private final int code;
    private final ErrorType errorType;
    private final HttpStatus httpStatus;

    @JsonValue
    public int getJsonValue() {
        return code;
    }

    @JsonCreator
    public static MyError fromJsonValue(Integer jsonValue) {
        return Optional.ofNullable(jsonValue)
                .flatMap(MyError::fromCode)
                .orElse(null);
    }

    public static Optional<MyError> fromCode(int code) {
        for (MyError e : values()) {
            if (e.getCode() == code) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

}


```

Extending `BaseException` with a custom one:

```java
@Getter
public abstract class MyException extends BaseException {

    final MyError myError;

    public MyException(MyError myError, String message) {
        this(myError, message, null);
    }

    public MyException(MyError myError, String message, Throwable cause) {
        super(message, cause, UUID.randomUUID().toString());
        this.myError = myError;
    }

}
```

An example exception:

```java
@Getter
public class AuthenticationFailedException extends MyException {

    public AuthenticationFailedException() {
        super(MyException.AUTHENTICATION_FAILED, "Authentication failed");
    }

}

```

### GlobalExceptionHandler

`@ControllerAdvice` component that provides centralized exception handling and ensures that all exceptions are caught
and transformed into consistent JSend responses

#### Example extension

```java
@ControllerAdvice
@Slf4j
public class MyExceptionHandler extends GlobalExceptionHandler {

    private final String COMPONENT_NAME = "my-component";

    @ExceptionHandler(MyException.class)
    public ResponseEntity<JSendResponse> handleMyException(MyException ex) {
        ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), ex.getmyError().getCode(), ex.errorInstanceId(), COMPONENT_NAME);
        log.error("Exception: {}", ex.myError);
        return ResponseEntity.status(ex.getmyError().getHttpStatus()).body(new JSendResponse(ex.getmyError().getErrorType().getJsendStatus(), errorDetails));
    }
}

```
