package aq.project.utils.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class HeaderValidatorHttpFilter extends OncePerRequestFilter {

    private static final String X_TRACE_ID_HEADER = "x-trace-id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if(request.getHeader(X_TRACE_ID_HEADER) == null || request.getHeader(X_TRACE_ID_HEADER).isBlank()) {
            String errorMessage = String.format("No [%s] header found in received request", X_TRACE_ID_HEADER);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
