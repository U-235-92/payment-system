package aq.project.util.keycloak;

import aq.project.dto.CreateUserRequest;
import aq.project.dto.UpdateUserRequest;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public final class KeycloakUtils {

    public static UserRepresentation getUserRepresentation(UpdateUserRequest updateUserRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(updateUserRequest.getIndividualData().getFirstName());
        userRepresentation.setLastName(updateUserRequest.getIndividualData().getLastName());
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(updateUserRequest)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(UpdateUserRequest updateUserRequest) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(updateUserRequest.getPassword());
        return credentialRepresentation;
    }

    public static UserRepresentation getUserRepresentation(CreateUserRequest createUserRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(createUserRequest.getIndividualData().getFirstName());
        userRepresentation.setLastName(createUserRequest.getIndividualData().getLastName());
        userRepresentation.setUsername(createUserRequest.getUsername());
        userRepresentation.setEmail(createUserRequest.getIndividualData().getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(createUserRequest)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(CreateUserRequest createUserRequest) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(createUserRequest.getPassword());
        return credentialRepresentation;
    }
}
