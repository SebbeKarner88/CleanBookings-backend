package com.example.cleanbookingsbackend.keycloak;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleAssignmentEntity;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleEntity;
import com.example.cleanbookingsbackend.keycloak.models.adminTokenEntity.KeycloakAdminTokenEntity;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.Credentials;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.NewUserEntity;
import com.example.cleanbookingsbackend.keycloak.models.userEntity.KeycloakUserEntity;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Service
public class KeycloakInitData {

    private final RestTemplate restTemplate;

    public KeycloakInitData(RestTemplate restTemplate, PasswordEncoder encoder) {
        this.restTemplate = restTemplate;
    }

    private final String REALM = "Karner";
    private final String CLIENT_ID = "0fc8c1c1-7ca8-40b9-8655-bc3a48e95540";
    private final String ROLE_CUSTOMER_ID = "bd446b9d-3e5e-4436-a804-a6582a44bda1";
    private final String ROLE_CLEANER_ID = "c0c42f0d-76f0-46ca-9b8a-93a7e1c82407";
    private final String ROLE_ADMIN_ID = "95873811-19bc-41ef-9ddf-8d7e45701938";
    private final String TEST_USER_ID = "91f08a18-94bb-4f48-9346-1612a903a304";
    private String aToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxODU4NzQsImlhdCI6MTY5ODE0OTg3NCwianRpIjoiYzU4MzBhYzUtNTA4ZC00YTExLWE3MDItOWUyN2ZlOWRmODQ0IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiY2ZlNjlhOWEtMzI5OC00N2NlLTgyYTUtNjQzMTMwMTA0ZWFlIiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6ImNmZTY5YTlhLTMyOTgtNDdjZS04MmE1LTY0MzEzMDEwNGVhZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4ifQ.XJSejUJF8OvlXYq5C3ovzYiYumUM2JWGqU7w5-iXfHpYVLknsf1uWGXB0wPkEuCO667wPcerZ7WfR1sird1RXKKphTBScUOCfB9vMoh4Qcmd6iI1-zt_6ttyNbm0tdieCqu-a6YDEi76acJh7wt2pHWL9lgwsprbYf3WiAiR2ftyB230qusBzV-Bwzr7O1Ko8ZkYE0lfEvkt0WOBEGhbL-oMuTPKyjSK79vj44CIfz1xFh87ZlMgARlniUHGg9rLsM0dnsLCym81otMMebiIVwApPHxxQRYmvjNG8xdoreLAI2ZAN55AHZuuAnbqyQQoOUuT8AlBbymlxgQbCA9eYQ";

    private PrivateCustomerEntity customer = new PrivateCustomerEntity(
            null,
            "Johnny",
            "Doe",
            null,
            CustomerType.PRIVATE,
            "Johnny Street 1",
            12345,
            "Johnny City",
            "076-250 45 23",
            "johnny.doe@aol.com",
            "password",
            null);

    @PostConstruct
    public void getKeycloakData() {


        try {
            // KeycloakAdminTokenEntity admin = getAdminTokenEntity("admin", "admin");
            // System.out.println(admin.toString());

            List<KeycloakUserEntity> users = getKeycloakUserEntities(aToken);
            System.out.println(users.toString());

            List<KeycloakRoleEntity> roles = getKeycloakRoleEntities(aToken);
            System.out.println(roles.toString());

            int createNewCustomerStatus = createNewCustomer(aToken, customer).value();
            System.out.println(createNewCustomerStatus);

            int assignRoleToUser = assignRoleToCustomer(aToken, Role.CUSTOMER, TEST_USER_ID).value();
            System.out.println(assignRoleToUser);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    // GET A ADMINENTITY CONTAINING A TOKEN TO BE ABLE TO REGISTER A NEW USER IN KEYCLOAK
    public KeycloakAdminTokenEntity getAdminTokenEntity(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");
            map.add("client_id", "admin-cli");

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


    // GET A LIST OF KEYCLOAKUSERENTITY FROM KEYCLOAK REALM AND RETURNING A LIST WITH USERS.
    public List<KeycloakUserEntity> getKeycloakUserEntities(String adminToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer " + adminToken);
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<List<KeycloakUserEntity>> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users",
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

    // GET ALL AVAILABLE KEYCLOAK ROLES ON CLIENT LEVEL.
    public List<KeycloakRoleEntity> getKeycloakRoleEntities(String adminToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer " + adminToken);
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<List<KeycloakRoleEntity>> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/clients/" + CLIENT_ID + "/roles",
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


    // CREATE A NEW CUSTOMER IN THE KEYCLOAK DB
    public HttpStatusCode createNewCustomer(String adminToken, PrivateCustomerEntity customer) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);

            Credentials[] credArr = {
                    new Credentials("password",
                            customer.getPassword(),
                            false)};

            NewUserEntity newUserBody = new NewUserEntity(
                    true,
                    customer.getEmailAddress(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmailAddress(),
                    credArr
            );

            HttpEntity<NewUserEntity> entity =
                    new HttpEntity<>(newUserBody, headers);

            ResponseEntity<?> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users",
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

    // ASSIGN ROLE TO CUSTOMER KEYCLOAK
    public HttpStatusCode assignRoleToCustomer(String adminToken, Role role, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);

            KeycloakRoleAssignmentEntity roleEntity = determineRole(role);

            //TODO: SOMETHING WRONG WITH THIS

            HttpEntity<KeycloakRoleAssignmentEntity> entity =
                    new HttpEntity<>(roleEntity, headers);

            ResponseEntity<?> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + userId + "/role-mappings/clients/" + CLIENT_ID,
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

    private KeycloakRoleAssignmentEntity determineRole(Role role) {
        return switch (role) {
            case CUSTOMER ->
                    new KeycloakRoleAssignmentEntity("bd446b9d-3e5e-4436-a804-a6582a44bda1", "client_customer");
            case ADMIN -> new KeycloakRoleAssignmentEntity("95873811-19bc-41ef-9ddf-8d7e45701938", "client_admin");
            case CLEANER -> new KeycloakRoleAssignmentEntity("c0c42f0d-76f0-46ca-9b8a-93a7e1c82407", "client_cleaner");
        };
    }

}
