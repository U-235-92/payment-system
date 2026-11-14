package aq.project.utils.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ClientErrorHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean isErrorResponse(
            HttpStatusCode statusCode
    ) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    public static void onClientErrorResponse(
           HttpRequest request,
           ClientHttpResponse response
    ) {
        String errorMessage = null;

        HttpStatusCode status = null;

        try {
            status = response.getStatusCode();

            byte[] responseBytes = response.getBody().readAllBytes();

            errorMessage = getErrorMessage(responseBytes);

        } catch (IOException e) {
            errorMessage = "Unexpected error occurred while handle error response";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if(status.is4xxClientError())
            throw new HttpClientErrorException(status, errorMessage);

        if(status.is5xxServerError())
            throw new HttpServerErrorException(status, errorMessage);
    }

    public static Mono<? extends Exception> onWebClientErrorResponse(
            ClientResponse response
    ) {
        return Mono.just(response.statusCode())
                .flatMap(status -> response
                        .body((clientHttpResponse, context) ->
                                DataBufferUtils.join(clientHttpResponse.getBody())
                                        .map(ClientErrorHandler::readByteArray)
                                        .map(ClientErrorHandler::getErrorMessage)
                                        .map(description -> getHttpErrorException(status, description))
                                        .switchIfEmpty(Mono.error(getHttpErrorException(status, null)))));
    }

    private static byte[] readByteArray(
            DataBuffer dataBuffer
    ) {
        byte[] bytes = null;

        try (InputStream inputStream = dataBuffer.asInputStream()) {
            bytes = inputStream.readAllBytes();

        } catch (IOException e) {
            log.error("{} exception occurred while process received error response. Exception message: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            bytes = new byte[]{};

        } finally {
            DataBufferUtils.release(dataBuffer);
        }
        return bytes;
    }

    private static String getErrorMessage(
            byte[] bytes
    ) {
        String errorDescription = null;

        if(bytes.length == 0) {
            errorDescription = "No response body provided";
        } else {
            try {
                JsonNode jsonTree = OBJECT_MAPPER.readTree(bytes);
                JsonNode messageNode = jsonTree.get("message");

                errorDescription = (messageNode == null || messageNode.isNull())
                        ? new String(bytes, StandardCharsets.UTF_8)
                        : messageNode.asString();

            } catch (JacksonException e) {
//            Suppressed: response contains no target data
                errorDescription = new String(bytes, StandardCharsets.UTF_8);
            }
        }
        return errorDescription;
    }

    private static Exception getHttpErrorException(
            HttpStatusCode status,
            String description
    ) {
        if(status.is4xxClientError())
            return getHttpClientErrorException(status, description);
        else
            return getHttpServerErrorException(status, description);
    }

    private static HttpClientErrorException getHttpClientErrorException(
            HttpStatusCode httpStatusCode,
            String errorDescription
    ) {
        if(errorDescription == null)
            return new HttpClientErrorException(httpStatusCode);
        return new HttpClientErrorException(httpStatusCode, errorDescription);
    }

    private static HttpServerErrorException getHttpServerErrorException(
            HttpStatusCode httpStatusCode,
            String errorDescription
    ) {
        if(errorDescription == null)
            return new HttpServerErrorException(httpStatusCode);
        return new HttpServerErrorException(httpStatusCode, errorDescription);
    }
}