package com.example.cleanbookingsbackend.keycloak.models.newUserEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewUserEntity {
    Boolean enabled;
    String email;
    String firstName;
    String lastName;
    String username;
    Credentials[] credentials;
}
