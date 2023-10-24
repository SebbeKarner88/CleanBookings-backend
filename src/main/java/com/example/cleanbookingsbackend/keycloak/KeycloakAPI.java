package com.example.cleanbookingsbackend.keycloak;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleAssignmentEntity;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleEntity;
import com.example.cleanbookingsbackend.keycloak.models.tokenEntity.KeycloakTokenEntity;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.Credentials;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.NewUserEntity;
import com.example.cleanbookingsbackend.keycloak.models.userEntity.KeycloakUserEntity;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static com.example.cleanbookingsbackend.enums.Role.*;


@Service
public class KeycloakAPI {

    private final RestTemplate restTemplate;

    public KeycloakAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private final String REALM = "Karner";
    private final String CLIENT_ID = "0fc8c1c1-7ca8-40b9-8655-bc3a48e95540";
    private final String CLIENT_SECRET = "v03vGy8VnToWtLw6fnGbhv9QZndODALx";
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin";
    private final String ROLE_CUSTOMER_ID = "bd446b9d-3e5e-4436-a804-a6582a44bda1";
    private final String ROLE_CLEANER_ID = "c0c42f0d-76f0-46ca-9b8a-93a7e1c82407";
    private final String ROLE_ADMIN_ID = "95873811-19bc-41ef-9ddf-8d7e45701938";
    private final String TEST_USER_ID = "91f08a18-94bb-4f48-9346-1612a903a304";
    private String ADMIN_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxODkzMDEsImlhdCI6MTY5ODE1MzMwMSwianRpIjoiZmZiMTJmNzItMTQ3Yi00ODY3LWI0ZjctZWE4Y2FhMmZmNzcwIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiZjc3ZDYxMDItNmY0ZS00NjFiLTlhOGMtZmM3YzljMTRhYWZkIiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6ImY3N2Q2MTAyLTZmNGUtNDYxYi05YThjLWZjN2M5YzE0YWFmZCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4ifQ.rlgV30XWp9p4B8a5f48vaOhyLnbEb9TsSIl9Pn2TM0Kp1PTf3gYrMLKsKJSnBlFgP9pmTrD7Vx-7s6NaYfnZJieEqES3FLz0X6mGsddyo48S4NNByV3oeEe2WfMX1ZzUjQt4nHfTFTx4r8SKZSEJZ954NPIXU7aEi2YabQtG55BwJJ9Qmv8pHyh-VplW5JpxqoDNorvZ-A71ui9EmSTuM4-iLit2CigcM1CsRPHGrGlPvrk5A73G0lnktc0mq1WvoYcUcCk-glNfKIgCsRC4uEvsHHJzqcjQFKfPBazVVkO-tpgxYrqvjIv-O-Nuit_3GUi-Bhlrs9wp8kim1w1-Jg";

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
            KeycloakTokenEntity adminTokenEntity = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD);
            ADMIN_TOKEN = adminTokenEntity.getAccess_token();
            System.out.println("ADMIN TOKEN: " + adminTokenEntity.toString());

            KeycloakTokenEntity userTokenEntity = loginKeycloak("sebbe","sebbe");
            System.out.println("USER TOKEN: " + userTokenEntity.toString());

            List<KeycloakUserEntity> users = getKeycloakUserEntities(ADMIN_TOKEN);
            System.out.println(users.toString());

            List<KeycloakRoleEntity> roles = getKeycloakRoleEntities(ADMIN_TOKEN);
            System.out.println(roles.toString());

            int createNewCustomerStatus = createNewUser(ADMIN_TOKEN, customer).value();
            System.out.println(createNewCustomerStatus);

            int assignRoleToUser = assignRoleToCustomer(ADMIN_TOKEN, CUSTOMER, TEST_USER_ID).value();
            System.out.println(assignRoleToUser);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    // GET A ADMINENTITY CONTAINING A TOKEN TO BE ABLE TO REGISTER A NEW USER IN KEYCLOAK
    public KeycloakTokenEntity getAdminTokenEntity(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");
            map.add("client_id", "admin-cli");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<KeycloakTokenEntity> response = restTemplate.exchange(
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
    public HttpStatusCode createNewUser(String adminToken, PrivateCustomerEntity customer) {
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

    // ASSIGN ROLE TO USER KEYCLOAK
    public HttpStatusCode assignRoleToCustomer(String adminToken, Role role, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            KeycloakRoleAssignmentEntity[] arr = {determineRole(role)};
            HttpEntity<KeycloakRoleAssignmentEntity[]> entity =
                    new HttpEntity<>(arr, headers);
            ResponseEntity<HttpStatusCode> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" +
                            userId + "/role-mappings/clients/" + CLIENT_ID,
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

    // LOGIN CALL FOR USER KEYCLOAK
    public KeycloakTokenEntity loginKeycloak(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");
            map.add("client_id", "karner-rest-api");
            map.add("client_secret", CLIENT_SECRET);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<KeycloakTokenEntity> response = restTemplate.exchange(
                    "http://localhost:8080/realms/" + REALM + "/protocol/openid-connect/token",
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

    private KeycloakRoleAssignmentEntity determineRole(Role role) {
        return switch (role) {
            case CUSTOMER -> new KeycloakRoleAssignmentEntity(ROLE_CUSTOMER_ID, "client_customer");
            case ADMIN -> new KeycloakRoleAssignmentEntity(ROLE_ADMIN_ID, "client_admin");
            case CLEANER -> new KeycloakRoleAssignmentEntity(ROLE_CLEANER_ID, "client_cleaner");
        };
    }

}
