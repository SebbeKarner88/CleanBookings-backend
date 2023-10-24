package com.example.cleanbookingsbackend.keycloak.models.roleEntity;

import lombok.Data;

@Data
public class KeycloakRoleEntity {

    String id;
    String name;
    String description;
    Boolean composite;
    Boolean clientRole;
    String containerId;
}
