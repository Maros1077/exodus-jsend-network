package cz.exodus.jsend.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.exodus.jsend.network.exception.ErrorDetails;
import cz.exodus.jsend.network.exception.ErrorType;
import cz.exodus.jsend.network.exception.JSendClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import cz.exodus.jsend.network.rest.JSendResponse;


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

    protected <T> Mono<T> executeRequest(String uri, Object request, Class<T> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .onStatus(this::isNotStatus200, this::handleError)
                .bodyToMono(createParameterizedType(responseType))
                .flatMap(response -> {
                    log.info("Client response: {} {}", uri, response);
                    if (!"success".equals(response.getStatus())) {
                        ErrorDetails error = convertDataToType(response.getData(), ErrorDetails.class);
                        return Mono.error(new JSendClientException(ErrorType.fromJsendStatus(response.getStatus()), HttpStatus.valueOf(200), error));
                    }
                    return Mono.just(convertDataToType(response.getData(), responseType));
                });
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
}

