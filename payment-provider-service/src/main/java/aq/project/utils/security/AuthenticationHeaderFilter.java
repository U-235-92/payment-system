package aq.project.utils.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if(isRequestContainsBasicAuthorizationHeader(request)) {
            filterChain.doFilter(request, response);
        } else {
            String traceId = genTraceId();
            int status = HttpStatus.UNAUTHORIZED.value();
            String errorMessage = "Received unauthorized request. Request hasn't got an [Authorization] header or contains no [Basic ] value";
            log.error("[{}]: {} {}", traceId, status, errorMessage);
            response.sendError(status, errorMessage);
        }
    }

    private boolean isRequestContainsBasicAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Basic ");
    }

    private static String genTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder traceId = new StringBuilder();
        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }
        return traceId.toString();
    }
}
