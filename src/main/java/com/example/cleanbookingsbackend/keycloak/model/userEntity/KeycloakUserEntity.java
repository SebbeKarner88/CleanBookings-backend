package com.example.cleanbookingsbackend.keycloak.model.userEntity;

import lombok.Data;

@Data
public class KeycloakUserEntity {

    String id;
    Long createdTimestamp;
    String username;
    Boolean enabled;
    Boolean totp;
    Boolean emailVerified;
    String firstName;
    String lastName;
    String email;
    String[] disableableCredentialTypes;
    String[] requiredActions;
    Long notBefore;
    KeycloakAccess access;

}
