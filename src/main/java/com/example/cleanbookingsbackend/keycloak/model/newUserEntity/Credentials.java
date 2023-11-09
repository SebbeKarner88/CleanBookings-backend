package com.example.cleanbookingsbackend.keycloak.model.newUserEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credentials {

    String type;
    String value;
    Boolean temporary;
}
