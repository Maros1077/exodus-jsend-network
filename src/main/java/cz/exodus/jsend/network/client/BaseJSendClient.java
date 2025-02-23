package cz.exodus.jsend.network.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.exodus.jsend.network.exception.ErrorDetails;
import cz.exodus.jsend.network.exception.ErrorType;
import cz.exodus.jsend.network.exception.JSendClientException;
import cz.exodus.jsend.network.model.Result;
import cz.exodus.jsend.network.rest.JSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
public abstract class BaseJSendClient {
    protected final WebClient webClient;
    protected final String serviceName;
    protected final ObjectMapper objectMapper;

    protected BaseJSendClient(WebClient webClient, String serviceName, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.serviceName = serviceName;
        this.objectMapper = objectMapper;
    }

    protected <T> Mono<T> executePostRequest(String uri, Object request, Class<T> responseType) {
        log.info("Sending POST request to URI: {}", uri);
        log.info("Request Body: {}", serializeRequest(request));
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .onStatus(this::isNotStatus200, this::handleError)
                .bodyToMono(createParameterizedType(responseType))
                .flatMap(response -> {
                    log.info("Client POST response: {} {}", uri, response);
                    if (!"success".equals(response.getStatus())) {
                        ErrorDetails error = convertDataToType(response.getData(), ErrorDetails.class);
                        return Mono.error(new JSendClientException(ErrorType.fromJsendStatus(response.getStatus()), HttpStatus.valueOf(200), error));
                    }
                    return Mono.just(convertDataToType(response.getData(), responseType));
                })
                .onErrorResume(JSendClientException.class, e -> {
                    throw new RuntimeException(e);
                });
    }

    protected <T> Result<T, JSendClientException> executePostRequestSync(String uri, Object request, Class<T> responseType) {
        try {
            T response = executePostRequest(uri, request, responseType).block();
            return Result.success(response);
        } catch (RuntimeException ex) {
            log.info("Client POST request error handling: {} {}", uri, ex.getMessage());
            Throwable cause = ex.getCause();
            if (cause instanceof JSendClientException) {
                return Result.failure((JSendClientException) cause);
            }
            return Result.failure(new JSendClientException(
                    ErrorType.ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new ErrorDetails(ex.getMessage(), 500)
            ));
        }
    }

    protected <T> Mono<T> executeGetRequest(String uri, Class<T> responseType) {
        log.info("Sending GET request to URI: {}", uri);
        return this.webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(this::isNotStatus200, this::handleError)
                .bodyToMono(this.createParameterizedType(responseType))
                .flatMap(response -> {
                    log.info("Client GET response: {} {}", uri, response);
                    if (!"success".equals(response.getStatus())) {
                        ErrorDetails error = convertDataToType(response.getData(), ErrorDetails.class);
                        return Mono.error(new JSendClientException(ErrorType.fromJsendStatus(response.getStatus()), HttpStatus.valueOf(200), error));
                    } else {
                        return Mono.just(convertDataToType(response.getData(), responseType));
                    }
                })
                .onErrorResume(JSendClientException.class, e -> {
                    throw new RuntimeException(e);
                });
    }

    protected <T> Result<T, JSendClientException> executeGetRequestSync(String uri, Class<T> responseType) {
        try {
            T response = executeGetRequest(uri, responseType).block();
            return Result.success(response);
        } catch (RuntimeException ex) {
            log.info("Client POST request error handling: {} {}", uri, ex.getMessage());
            Throwable cause = ex.getCause();
            if (cause instanceof JSendClientException) {
                return Result.failure((JSendClientException) cause);
            }
            return Result.failure(new JSendClientException(
                    ErrorType.ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new ErrorDetails(ex.getMessage(), 500)
            ));
        }
    }

    protected <T> Mono<T> executePutRequest(String uri, Object request, Class<T> responseType) {
        log.info("Sending PUT request to URI: {}", uri);
        log.info("Request Body: {}", serializeRequest(request));
        return this.webClient.put()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .onStatus(this::isNotStatus200, this::handleError)
                .bodyToMono(this.createParameterizedType(responseType))
                .flatMap(response -> {
                    log.info("Client PUT response: {} {}", uri, response);
                    if (!"success".equals(response.getStatus())) {
                        ErrorDetails error = convertDataToType(response.getData(), ErrorDetails.class);
                        return Mono.error(new JSendClientException(ErrorType.fromJsendStatus(response.getStatus()), HttpStatus.valueOf(200), error));
                    } else {
                        return Mono.just(convertDataToType(response.getData(), responseType));
                    }
                })
                .onErrorResume(JSendClientException.class, e -> {
                    throw new RuntimeException(e);
                });
    }

    protected <T> Result<T, JSendClientException> executePutRequestSync(String uri, Object request, Class<T> responseType) {
        try {
            T response = executePutRequest(uri, request, responseType).block();
            return Result.success(response);
        } catch (RuntimeException ex) {
            log.info("Client POST request error handling: {} {}", uri, ex.getMessage());
            Throwable cause = ex.getCause();
            if (cause instanceof JSendClientException) {
                return Result.failure((JSendClientException) cause);
            }
            return Result.failure(new JSendClientException(
                    ErrorType.ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new ErrorDetails(ex.getMessage(), 500)
            ));
        }
    }

    private <T> ParameterizedTypeReference<JSendResponse<T>> createParameterizedType(Class<T> type) {
        return new ParameterizedTypeReference<JSendResponse<T>>() {
        };
    }

    private <T> T convertDataToType(Object data, Class<T> targetType) {
        return objectMapper.convertValue(data, targetType);
    }

    public Mono<Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(JSendResponse.class)
                .flatMap(errorBody -> {
                            log.error("Client error response: {} {}", response.request(), errorBody);
                            ErrorDetails error = convertDataToType(errorBody.getData(), ErrorDetails.class);
                            return Mono.error(new JSendClientException(ErrorType.fromJsendStatus(errorBody.getStatus()), HttpStatus.valueOf(response.statusCode().value()), error));
                        }
                );
    }

    private boolean isNotStatus200(HttpStatusCode status) {
        return (status.value() != 200);
    }

    private String serializeRequest(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error serializing request: {}", e.getMessage(), e);
            return request.toString();
        }
    }
}

