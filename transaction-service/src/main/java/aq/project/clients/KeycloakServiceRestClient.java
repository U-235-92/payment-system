package aq.project.clients;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_SECRET;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;

public interface KeycloakServiceRestClient {

    default String getAdminJwt(String adminId, String adminSecret, ObjectMapper objectMapper) {
        LinkedMultiValueMap<String, String> formUrlEncoded = new LinkedMultiValueMap<>();
        formUrlEncoded.add(CLIENT_ID, adminId);
        formUrlEncoded.add(CLIENT_SECRET, adminSecret);
        formUrlEncoded.add(GRANT_TYPE, CLIENT_CREDENTIALS.getValue());
        return objectMapper
                .readTree(getAdminJwtInternal(formUrlEncoded))
                .get("access_token")
                .asString();
    }

    @PostExchange(
            value = "${service.keycloak.endpoints.token}",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    String getAdminJwtInternal(@RequestBody LinkedMultiValueMap<String, String> formUrlEncoded);
}
