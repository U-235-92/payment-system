package aq.project.util.http;

import org.springframework.http.HttpStatusCode;

public abstract class HttpUtils {

    public static boolean isErrorStatusCode(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
}
