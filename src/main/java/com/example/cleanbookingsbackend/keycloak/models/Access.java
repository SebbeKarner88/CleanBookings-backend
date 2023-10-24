package com.example.cleanbookingsbackend.keycloak.models;

import lombok.Data;

@Data
public class Access {
    Boolean manageGroupMembership;
    Boolean view;
    Boolean mapRoles;
    Boolean impersonate;
    Boolean manage;
}
