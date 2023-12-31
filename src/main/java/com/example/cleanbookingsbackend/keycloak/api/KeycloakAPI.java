package com.example.cleanbookingsbackend.keycloak.api;

import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.keycloak.model.roleEntity.KeycloakRoleAssignmentEntity;
import com.example.cleanbookingsbackend.keycloak.model.roleEntity.KeycloakRoleEntity;
import com.example.cleanbookingsbackend.keycloak.model.tokenEntity.KeycloakTokenEntity;
import com.example.cleanbookingsbackend.keycloak.model.newUserEntity.Credentials;
import com.example.cleanbookingsbackend.keycloak.model.newUserEntity.NewUserEntity;
import com.example.cleanbookingsbackend.keycloak.model.userEntity.KeycloakUserEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    @Value("${KC_CLIENT_NAME}")
    private String CLIENT_NAME;
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


    //###################################################################################
    //############################## HELPER METHODS #####################################
    //###################################################################################

    public String getUserRole(Jwt jwt) {
        String role = "";
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

        if (resourceAccess.containsKey(CLIENT_NAME)) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(CLIENT_NAME);
            List<String> roles = (List<String>) clientAccess.get("roles");

            if (roles.isEmpty())
                throw new RuntimeException("The user has no roles.");

            role = roles.get(0);
        }
        return role;
    }

    public PrivateCustomerEntity addCustomerKeycloak(PrivateCustomerEntity customer, String password) throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        try {
            createNewCustomer(adminToken, customer, password).is2xxSuccessful();
            List<KeycloakUserEntity> addedUser = getKeycloakUserEntities(adminToken)
                    .stream()
                    .filter(entity -> entity.getUsername().equalsIgnoreCase(customer.getEmailAddress()))
                    .toList();
            if (addedUser.isEmpty()) {
                throw new RuntimeException("User could not be added to Keycloak");
            }
            String id = addedUser.get(0).getId();
            customer.setId(id);
            assignRoleToCustomer(adminToken, id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return customer;
    }

    public EmployeeEntity addEmployeeKeycloak(EmployeeEntity employee) throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        try {
            createNewEmployee(adminToken, employee).is2xxSuccessful();
            List<KeycloakUserEntity> addedUser = getKeycloakUserEntities(adminToken)
                    .stream()
                    .filter(entity -> entity.getUsername().equalsIgnoreCase(employee.getEmailAddress()))
                    .toList();
            if (addedUser.isEmpty()) {
                throw new RuntimeException("User could not be added to Keycloak");
            }
            String id = addedUser.get(0).getId();
            employee.setId(id);
            assignRoleToEmployee(adminToken, employee.getRole(), id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return employee;
    }

    public PrivateCustomerEntity updateCustomerKeycloak(PrivateCustomerEntity customer) throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        try{
            updateCustomer(adminToken, customer);
        } catch (Exception e){
            throw new RuntimeException("Failed to update customer. ERRORCODE: " + e);
        }
        return customer;
    }

    public EmployeeEntity updateEmployeeKeycloak(EmployeeEntity employee) throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        try{
            updateEmployee(adminToken, employee);
        } catch (Exception e){
            throw new RuntimeException("Failed to update customer. ERRORCODE: " + e);
        }
        return employee;
    }

    public HttpStatusCode deleteUserKeycloak(String userId) throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        HttpStatusCode status;
        try{
            status = deleteUser(adminToken, userId);
        } catch (Exception e){
            throw new RuntimeException("Failed to update customer. ERRORCODE: " + e);
        }
        return status;
    }

    public HttpStatusCode changePasswordKeycloak(String userId, String password)
            throws RuntimeException {
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        HttpStatusCode status;
        try{
            status = changePasswordUser(adminToken, userId, password);
        } catch (Exception e){
            throw new RuntimeException("Failed to update customer. ERRORCODE: " + e);
        }
        return status;
    }

    private KeycloakRoleAssignmentEntity determineRole(Role role)
            throws IllegalArgumentException {
        return switch (role) {
            case ADMIN -> new KeycloakRoleAssignmentEntity(ROLE_ADMIN_ID, "client_admin");
            case CLEANER -> new KeycloakRoleAssignmentEntity(ROLE_CLEANER_ID, "client_cleaner");
            case CUSTOMER -> throw new IllegalArgumentException("Invalid role");
        };
    }



    //###################################################################################
    //############################# API CALLS ###########################################
    //###################################################################################

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

    // GET A LIST OF AVAILABLE KEYCLOAK ROLEENTITY ON CLIENT LEVEL.
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
    public HttpStatusCode createNewCustomer(String adminToken, PrivateCustomerEntity customer, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            HttpEntity<NewUserEntity> entity = getNewUserEntityHttpEntity(customer, password, headers);
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

    private static HttpEntity<NewUserEntity> getNewUserEntityHttpEntity(PrivateCustomerEntity customer, String password, HttpHeaders headers) {
        Credentials[] credArr = {
                new Credentials("password",
                        password,
                        false)};
        NewUserEntity newUserBody = new NewUserEntity(
                true,
                customer.getEmailAddress(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmailAddress(),
                credArr
        );
        return new HttpEntity<>(newUserBody, headers);
    }

    // CREATE A NEW CUSTOMER IN THE KEYCLOAK DB
    public HttpStatusCode createNewEmployee(String adminToken, EmployeeEntity employee) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            Credentials[] credArr = {
                    new Credentials("password",
                            "password",
                            false)};
            NewUserEntity newUserBody = new NewUserEntity(
                    true,
                    employee.getEmailAddress(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmailAddress(),
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
    public HttpStatusCode assignRoleToCustomer(String adminToken, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            KeycloakRoleAssignmentEntity[] arr = {new KeycloakRoleAssignmentEntity(ROLE_CUSTOMER_ID, "client_customer")};
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

    // ASSIGN ROLE TO EMPLOYEE
    public HttpStatusCode assignRoleToEmployee(String adminToken, Role role, String employeeId)
            throws IllegalArgumentException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            KeycloakRoleAssignmentEntity[] arr = {determineRole(role)};
            HttpEntity<KeycloakRoleAssignmentEntity[]> entity =
                    new HttpEntity<>(arr, headers);
            ResponseEntity<HttpStatusCode> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" +
                            employeeId + "/role-mappings/clients/" + CLIENT_ID,
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
            map.add("client_id", CLIENT_NAME);
            map.add("client_secret", CLIENT_SECRET);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<KeycloakTokenEntity> response = restTemplate.exchange(
                    "http://localhost:8080/realms/" + REALM + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            return response.getBody(); // 204 IS SUCCESS

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // CALL FOR USING REFRESH TOKEN TO GET NEW ACCESS TOKEN
    public KeycloakTokenEntity refreshToken(String refreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("refresh_token", refreshToken);
            map.add("grant_type", "refresh_token");
            map.add("client_id", CLIENT_NAME);
            map.add("client_secret", CLIENT_SECRET);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<KeycloakTokenEntity> response = restTemplate.exchange(
                    "http://localhost:8080/realms/" + REALM + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            return response.getBody(); // 200 IS SUCCESS

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // LOGOUT USER
    public HttpStatusCode logoutKeycloak(String userRefreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", CLIENT_NAME);
            map.add("client_secret", CLIENT_SECRET);
            map.add("refresh_token", userRefreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8080/realms/" + REALM + "/protocol/openid-connect/logout",
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

    // DELETE EMPLOYEE/CUSTOMER BY ID
    public HttpStatusCode deleteUser(String adminToken, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<HttpStatusCode> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + userId,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getStatusCode(); // 204 IS SUCCESS
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public HttpStatusCode updateCustomer(String adminToken, PrivateCustomerEntity customer) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            Credentials[] credArr = {};
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
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + customer.getId(),
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

    public HttpStatusCode updateEmployee(String adminToken, EmployeeEntity employee) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            Credentials[] credArr = {};
            NewUserEntity newUserBody = new NewUserEntity(
                    true,
                    employee.getEmailAddress(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmailAddress(),
                    credArr
            );
            HttpEntity<NewUserEntity> entity =
                    new HttpEntity<>(newUserBody, headers);
            ResponseEntity<?> response = restTemplate.exchange(
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + employee.getId(),
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
}
