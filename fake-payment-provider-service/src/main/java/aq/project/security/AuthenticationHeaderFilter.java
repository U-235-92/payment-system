package aq.project.security;

import aq.project.utils.telemetry.TraceFactory;
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
            String traceId = TraceFactory.genTraceId();

            int status = HttpStatus.UNAUTHORIZED.value();

            String errorMessage = "Received unauthorized request. " +
                    "Request hasn't got an [Authorization] header or contains no [Basic ] value";

            log.error("[{}]: {} {}", traceId, status, errorMessage);

            response.sendError(status, errorMessage);
        }
    }

    private boolean isRequestContainsBasicAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Basic ");
    }
}
