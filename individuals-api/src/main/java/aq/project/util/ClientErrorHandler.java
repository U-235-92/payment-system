package aq.project.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
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
@Component
@RequiredArgsConstructor
public class ClientErrorHandler {

    private final ObjectMapper objectMapper;

    public boolean isErrorResponse(
            HttpStatusCode statusCode
    ) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    public Mono<? extends Exception> onWebClientErrorResponse(
            ClientResponse response
    ) {
        return Mono.just(response.statusCode())
                .flatMap(status -> response
                        .body((clientHttpResponse, context) ->
                                DataBufferUtils.join(clientHttpResponse.getBody())
                                        .map(this::readByteArray)
                                        .map(this::getErrorMessage)
                                        .map(description -> getHttpErrorException(status, description))
                                        .switchIfEmpty(Mono.error(getHttpErrorException(status, null)))));
    }

    private byte[] readByteArray(
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

    private String getErrorMessage(
            byte[] bytes
    ) {
        String errorMessage = null;

        if(bytes.length == 0) {
            errorMessage = "No response body provided";
        } else {
            try {
                JsonNode jsonTree = objectMapper.readTree(bytes);
                JsonNode messageNode = jsonTree.get("message");

                errorMessage = (messageNode == null || messageNode.isNull())
                        ? new String(bytes, StandardCharsets.UTF_8)
                        : messageNode.asString();

            } catch (JacksonException e) {
//            Suppressed: response contains no target data
                errorMessage = new String(bytes, StandardCharsets.UTF_8);
            }
        }
        return errorMessage;
    }

    private Exception getHttpErrorException(
            HttpStatusCode status,
            String errorMessage
    ) {
        if(status.is4xxClientError())
            return getHttpClientErrorException(status, errorMessage);
        else
            return getHttpServerErrorException(status, errorMessage);
    }

    private HttpClientErrorException getHttpClientErrorException(
            HttpStatusCode httpStatusCode,
            String errorMessage
    ) {
        if(errorMessage == null)
            return new HttpClientErrorException(httpStatusCode);
        return new HttpClientErrorException(httpStatusCode, errorMessage);
    }

    private HttpServerErrorException getHttpServerErrorException(
            HttpStatusCode httpStatusCode,
            String errorMessage
    ) {
        if(errorMessage == null)
            return new HttpServerErrorException(httpStatusCode);
        return new HttpServerErrorException(httpStatusCode, errorMessage);
    }
}
