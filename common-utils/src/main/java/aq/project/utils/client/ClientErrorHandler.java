package aq.project.utils.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
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

    private static String getErrorMessage(
            byte[] bytes
    ) {
        String errorMessage = null;

        if(bytes.length == 0) {
            errorMessage = "No response body provided";
        } else {
            try {
                JsonNode jsonTree = OBJECT_MAPPER.readTree(bytes);
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
}