package aq.project.util.keycloak;

import aq.project.dto.CreateUserDTO;
import aq.project.dto.UpdateUserDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public final class KeycloakUtils {

    public static UserRepresentation getUserRepresentation(UpdateUserDTO updateUserDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(updateUserDTO.getIndividualData().getFirstName());
        userRepresentation.setLastName(updateUserDTO.getIndividualData().getLastName());
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(updateUserDTO)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(UpdateUserDTO updateUserDTO) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(updateUserDTO.getPassword());
        return credentialRepresentation;
    }

    public static UserRepresentation getUserRepresentation(CreateUserDTO createUserDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(createUserDTO.getIndividualData().getFirstName());
        userRepresentation.setLastName(createUserDTO.getIndividualData().getLastName());
        userRepresentation.setUsername(createUserDTO.getUsername());
        userRepresentation.setEmail(createUserDTO.getIndividualData().getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(createUserDTO)));
        return userRepresentation;
    }

    private static CredentialRepresentation getCredentialRepresentation(CreateUserDTO createUserDTO) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(createUserDTO.getPassword());
        return credentialRepresentation;
    }
}
