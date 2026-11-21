package aq.project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        checkAuthentication(authentication);

        return authentication;
    }

    private void checkAuthentication(Authentication authentication) {
        if(authentication == null)
            throw new NullPointerException("Authentication which represents authenticated merchant is null");
    }
}
