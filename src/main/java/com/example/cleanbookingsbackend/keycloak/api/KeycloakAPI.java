package com.example.cleanbookingsbackend.keycloak.api;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Service
public class KeycloakAPI {

    private final RestTemplate restTemplate;

    public KeycloakAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${KC_REALM}")
    private String REALM;
    @Value("${KC_CLIENT_ID}")
    private String CLIENT_ID;
    @Value("${KC_CLIENT_SECRET}")
    private String CLIENT_SECRET;
    @Value("${KC_ADMIN_USERNAME}")
    private String ADMIN_USERNAME;
    @Value("${KC_ADMIN_PASSWORD}")
    private String ADMIN_PASSWORD;
    @Value("${KC_ROLE_CUSTOMER_ID}")
    private String ROLE_CUSTOMER_ID;
    @Value("${KC_ROLE_CLEANER_ID}")
    private String ROLE_CLEANER_ID;
    @Value("${KC_ROLE_ADMIN_ID}")
    private String ROLE_ADMIN_ID;
    @Value("${KC_TEST_USER_ID}")
    private String TEST_USER_ID;

    private String ADMIN_TOKEN;
    private String USER_TOKEN;

    private PrivateCustomerEntity testCustomer = new PrivateCustomerEntity(
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

            KeycloakTokenEntity userTokenEntity = loginKeycloak("sebbe", "sebbe");
            USER_TOKEN = userTokenEntity.getAccess_token();
            System.out.println("USER TOKEN: " + userTokenEntity.toString());

            List<KeycloakUserEntity> users = getKeycloakUserEntities(ADMIN_TOKEN);
            System.out.println(users.toString());

            List<KeycloakRoleEntity> roles = getKeycloakRoleEntities(ADMIN_TOKEN);
            System.out.println(roles.toString());

          //  int createNewCustomerStatus = createNewUser(ADMIN_TOKEN, testCustomer).value();
          //  System.out.println(createNewCustomerStatus);

          //  int assignRoleToUser = assignRoleToCustomer(ADMIN_TOKEN, CUSTOMER, TEST_USER_ID).value();
          //  System.out.println(assignRoleToUser);

          //  int changePasswordUser = changePasswordUser(ADMIN_TOKEN, TEST_USER_ID, "nytt").value();
          //  System.out.println(changePasswordUser);

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
            return response.getStatusCode(); // 201 IS SUCCESS
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
            return response.getStatusCode(); // 204 IS SUCCESS
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

    // UPDATING PASSWORD ON USER KEYCLOAK
    public HttpStatusCode changePasswordUser(String adminToken, String userId, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            Credentials cred = new Credentials(
                    "password",
                    password,
                    false);

            HttpEntity<Credentials> entity =
                    new HttpEntity<>(cred, headers);
            ResponseEntity<HttpStatusCode> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + userId + "/reset-password",
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getStatusCode(); // 204 IS SUCCESS
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
