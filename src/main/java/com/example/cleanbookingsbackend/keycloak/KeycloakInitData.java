package com.example.cleanbookingsbackend.keycloak;

import com.example.cleanbookingsbackend.keycloak.models.adminTokenEntity.KeycloakAdminTokenEntity;
import com.example.cleanbookingsbackend.keycloak.models.userEntity.KeycloakUserEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Service
public class KeycloakInitData {

    private final RestTemplate restTemplate;

    public KeycloakInitData(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void getKeycloakData() {



        try {
            List<KeycloakUserEntity> users = getUsers();
            KeycloakAdminTokenEntity admin = getAdminToken();
            System.out.println(users.toString());
            System.out.println(admin.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }




    // GET A LIST OF KEYCLOAKUSERENTITY FROM KEYCLOAK REALM AND RETURNING A LIST WITH USERS.
    public List<KeycloakUserEntity> getUsers() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxNzQwOTMsImlhdCI6MTY5ODEzODA5MywianRpIjoiOWYyNDhkZTQtY2M3Ny00NTA0LWEyM2EtN2EwYzcyOTdlMGRjIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiZjRjMGU1NjQtNTAwOS00MzFhLWI5NjMtMmZhZTk1ZDNhYzU3IiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6ImY0YzBlNTY0LTUwMDktNDMxYS1iOTYzLTJmYWU5NWQzYWM1NyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4ifQ.ZPrrx5ebUQBZH5WDFIQ1Xn_D_QI4FxcY-KV2rAmCFtOm7rvFZTCKg_07scMpcQ0CZCMxor7LAfwosc1fA4dacXC0rZskILXS-A5ZpmSz_UpQi7eE9-m_sR7K31cpNsGSSyTnnB8S-vFn529xFJuoDZYl31isX5xatvPiKAeT76oD5M1BDnceNb9Bq7wNjIhjHMB-mMS7pE7RlaQyqxtI2EtwxNu0q4_1ZpSuvWvqnxeZSWNO3ypbc_c74Lbq74LZ_kIgMPDtwJQPP1ayog0AW9AZLKhz-tRBiuoiCUW1vp-WtxUKW_KzA75711lXhPx8svXYZNvHYDOS_J6I1aBvNw");

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<List<KeycloakUserEntity>> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/Karner/users",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            return response.getBody();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    // GET A ADMINENTITY CONTAINING A TOKEN TO BE ABLE TO REGISTER A NEW USER IN KEYCLOAK
    public KeycloakAdminTokenEntity getAdminToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username","admin");
            map.add("password","admin");
            map.add("grant_type","password");
            map.add("client_id","admin-cli");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<KeycloakAdminTokenEntity> response = restTemplate.exchange(
                    "http://localhost:8080/realms/master/protocol/openid-connect/token",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            return response.getBody();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }



}
