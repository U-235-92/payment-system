package aq.project.util.keycloak;

import aq.project.dto.CreateUserEvent;
import aq.project.dto.UpdateUserEvent;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public final class KeycloakUtils {

    public static UserRepresentation getUserRepresentation(UpdateUserEvent updateUserEvent) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(updateUserEvent.getIndividualData().getFirstName());
        userRepresentation.setLastName(updateUserEvent.getIndividualData().getLastName());
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(updateUserEvent)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(UpdateUserEvent updateUserEvent) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(updateUserEvent.getPassword());
        return credentialRepresentation;
    }

    public static UserRepresentation getUserRepresentation(CreateUserEvent createUserEvent) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(createUserEvent.getIndividualData().getFirstName());
        userRepresentation.setLastName(createUserEvent.getIndividualData().getLastName());
        userRepresentation.setUsername(createUserEvent.getUsername());
        userRepresentation.setEmail(createUserEvent.getIndividualData().getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(createUserEvent)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(CreateUserEvent createUserEvent) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(createUserEvent.getPassword());
        return credentialRepresentation;
    }
}
