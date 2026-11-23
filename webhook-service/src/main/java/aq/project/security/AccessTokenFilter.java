package aq.project.security;

import aq.project.utils.security.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;

import static aq.project.controller.WebhookRestControllerApi.PATH_CONFIRM_OPERATION_WEBHOOK;

@Slf4j
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_HASH_HEADER = "x-access-token-hash";

    private static final String ACCESS_TOKEN_HASH_ALG = "x-access-token-hash-alg";

    private final String serviceAccessTokenKey;

    private final String serviceAccessTokenHashAlg;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        log.debug(">>>>> Received request on path: {}", requestPath);

        if(isRequestedInvalidPath(requestPath)) {
            log.error("Request contains invalid target path: [{}]", requestPath);

            int statusCode = HttpStatus.BAD_REQUEST.value();

            String errorMessage = String.format("Request contains invalid target path: [%s]", requestPath);

            response.sendError(statusCode, errorMessage);

            return;
        }

        if(isRequiredHeadersNotFound(request)) {
            log.error("Request hasn't got any required headers: [{}] and [{}]",
                    ACCESS_TOKEN_HASH_HEADER, ACCESS_TOKEN_HASH_ALG);

            int statusCode = HttpStatus.BAD_REQUEST.value();

            String errorMessage = String.format("Request hasn't got any required headers: [%s] and [%s]",
                    ACCESS_TOKEN_HASH_HEADER, ACCESS_TOKEN_HASH_ALG);

            response.sendError(statusCode, errorMessage);

            return;
        }

        String requestAccessTokenHashAlg = request.getHeader(ACCESS_TOKEN_HASH_ALG);

        if(!requestAccessTokenHashAlg.equals(serviceAccessTokenHashAlg)) {
            log.error("Request contains not supported access token hash algorithm: [{}]. " +
                            "Supported hash algorithm: [{}]",
                    requestAccessTokenHashAlg, serviceAccessTokenHashAlg);

            int statusCode = HttpStatus.BAD_REQUEST.value();

            String errorMessage = String.format("Request contains not supported access token hash algorithm: [%s]. " +
                            "Supported hash algorithm is: [%s]",
                    requestAccessTokenHashAlg, serviceAccessTokenHashAlg);

            response.sendError(statusCode, errorMessage);

            return;
        }

        String requestAccessTokenHash = request.getHeader(ACCESS_TOKEN_HASH_HEADER);

        try {
            String serviceAccessTokenHash = SecurityUtil.getHash(serviceAccessTokenKey, serviceAccessTokenHashAlg);

            if(MessageDigest.isEqual(requestAccessTokenHash.getBytes(), serviceAccessTokenHash.getBytes())) {
                log.debug(">>>>> Success handle of request on path: {}", requestPath);

                filterChain.doFilter(request, response);

            } else {
                log.error("Request contains invalid [{}] header value", ACCESS_TOKEN_HASH_HEADER);

                int statusCode = HttpStatus.UNAUTHORIZED.value();

                String errorMessage = String.format("Request contains invalid [%s] header value",
                        ACCESS_TOKEN_HASH_HEADER);

                response.sendError(statusCode, errorMessage);
            }
        } catch (RuntimeException e) {
            log.error("Error occurred while validating access token. Received invalid hash algorithm: [{}]. " +
                            "Check service setting. Exception message: {}",
                    serviceAccessTokenHashAlg, e.getCause().getMessage());

            int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

            String errorMessage = String.format("Error occurred while validating access token. " +
                            "Received invalid hash algorithm: [%s]. Check service setting. Exception message: %s",
                    serviceAccessTokenHashAlg, e.getCause().getMessage());

            response.sendError(statusCode, errorMessage);
        }
    }

    private boolean isRequestedInvalidPath(String path) {
        return !path.startsWith(PATH_CONFIRM_OPERATION_WEBHOOK);
    }

    private boolean isRequiredHeadersNotFound(
            HttpServletRequest request
    ) {
        String accessTokenHashHeader = request.getHeader(ACCESS_TOKEN_HASH_HEADER);
        String accessTokenHashAlgHeader = request.getHeader(ACCESS_TOKEN_HASH_ALG);

        return accessTokenHashHeader == null || accessTokenHashAlgHeader == null;
    }
}