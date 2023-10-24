package com.example.cleanbookingsbackend.keycloak;

import com.example.cleanbookingsbackend.keycloak.models.adminTokenEntity.KeycloakAdminTokenEntity;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.Credentials;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.NewUserEntity;
import com.example.cleanbookingsbackend.keycloak.models.userEntity.KeycloakUserEntity;
import jakarta.annotation.PostConstruct;
import org.json.simple.JSONArray;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.simple.JSONObject;

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
            /*

            List<KeycloakUserEntity> users = getUsers();
            KeycloakAdminTokenEntity admin = getAdminToken();
            System.out.println(users.toString());
            System.out.println(admin.toString());
            System.out.println(createNewUser().value());

            */
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }




    // GET A LIST OF KEYCLOAKUSERENTITY FROM KEYCLOAK REALM AND RETURNING A LIST WITH USERS.
    public List<KeycloakUserEntity> getUsers() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxNzg2OTUsImlhdCI6MTY5ODE0MjY5NSwianRpIjoiZTE4NDUwYTEtMDdmMS00ZjQ3LTg5MjMtZTFmMDczNDY0ODczIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiODE5MDQzMjYtODlhOC00ZjAyLTg4YmYtNmRjMzEyYzc1Mjg5IiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjgxOTA0MzI2LTg5YTgtNGYwMi04OGJmLTZkYzMxMmM3NTI4OSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4ifQ.dHS6LOuwgS-DWm4uPKUyJrqVmGXkoV5sGf0zd9wvzx2t1b-EkfXYA1_g0WOn7u--SAp8eFd0vHGKo8YHEQfH9Mpjg3kMwFAfEJuVc6NXNDlMlWsQnCvNFwmSDvbyNRycRvDVoqrNMg34MOEAqYfVDEKN-fRvRBdUYQpjGravZpP_FKwzwI-xIqnPfVuckw7nfwiIn0f_f92EInLYv-ZEotCiyyXcCQhgAA7KjZVQvERVxUT-PqNdJ4U7ngjJfDCfazL_dnRXl_PU1tTyCiON9dj_HN4AsYWgUHFeDkR2_VYFeblCxzJ7lca3eILoZunR95cPZneIfYccoBb_7qVagQ");
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

    // CREATE A NEW USER IN THE KEYCLOAK DB
    public HttpStatusCode createNewUser() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxNzg2OTUsImlhdCI6MTY5ODE0MjY5NSwianRpIjoiZTE4NDUwYTEtMDdmMS00ZjQ3LTg5MjMtZTFmMDczNDY0ODczIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiODE5MDQzMjYtODlhOC00ZjAyLTg4YmYtNmRjMzEyYzc1Mjg5IiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjgxOTA0MzI2LTg5YTgtNGYwMi04OGJmLTZkYzMxMmM3NTI4OSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4ifQ.dHS6LOuwgS-DWm4uPKUyJrqVmGXkoV5sGf0zd9wvzx2t1b-EkfXYA1_g0WOn7u--SAp8eFd0vHGKo8YHEQfH9Mpjg3kMwFAfEJuVc6NXNDlMlWsQnCvNFwmSDvbyNRycRvDVoqrNMg34MOEAqYfVDEKN-fRvRBdUYQpjGravZpP_FKwzwI-xIqnPfVuckw7nfwiIn0f_f92EInLYv-ZEotCiyyXcCQhgAA7KjZVQvERVxUT-PqNdJ4U7ngjJfDCfazL_dnRXl_PU1tTyCiON9dj_HN4AsYWgUHFeDkR2_VYFeblCxzJ7lca3eILoZunR95cPZneIfYccoBb_7qVagQ");

            Credentials[] credArr = {new Credentials("password", "johndoe", false)};

            NewUserEntity newUserBody = new NewUserEntity(
                    true,
                    "JohnDoe@johndoe.com",
                    "john",
                    "Doe",
                    "johndoe",
                    credArr
            );

            HttpEntity<NewUserEntity> entity =
                    new HttpEntity<>(newUserBody, headers);

            ResponseEntity<?> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/Karner/users",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            return response.getStatusCode();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }




}
