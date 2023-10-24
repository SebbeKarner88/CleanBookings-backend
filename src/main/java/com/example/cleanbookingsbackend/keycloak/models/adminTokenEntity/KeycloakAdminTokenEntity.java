package com.example.cleanbookingsbackend.keycloak.models.adminTokenEntity;

import lombok.Data;

@Data
public class KeycloakAdminTokenEntity {

    String access_token;
    Long expires_in;
    Long refresh_expires_in;
    String refresh_token;
    String token_type;
    Long not_before_policy;
    String session_state;
    String scope;

}
