package com.example.cleanbookingsbackend.keycloak.model.userEntity;

import lombok.Data;

@Data
public class KeycloakAccess {
    Boolean manageGroupMembership;
    Boolean view;
    Boolean mapRoles;
    Boolean impersonate;
    Boolean manage;
}
