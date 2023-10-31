package com.example.cleanbookingsbackend.keycloak.api;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleAssignmentEntity;
import com.example.cleanbookingsbackend.keycloak.models.roleEntity.KeycloakRoleEntity;
import com.example.cleanbookingsbackend.keycloak.models.tokenEntity.KeycloakTokenEntity;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.Credentials;
import com.example.cleanbookingsbackend.keycloak.models.newUserEntity.NewUserEntity;
import com.example.cleanbookingsbackend.keycloak.models.userEntity.KeycloakUserEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
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

import static com.example.cleanbookingsbackend.enums.Role.CLEANER;

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
    @Value("${KC_TEST_CUSTOMER_ID}")
    private String TEST_CUSTOMER_ID;
    @Value("${KC_TEST_CUSTOMER_USERNAME}")
    private String TEST_CUSTOMER_USERNAME;
    @Value("${KC_TEST_CUSTOMER_PASSWORD}")
    private String TEST_CUSTOMER_PASSWORD;
    @Value("${KC_TEST_EMPLOYEE_ID}")
    private String TEST_EMPLOYEE_ID;


    private String ADMIN_TOKEN;
    private String USER_TOKEN;
    private String USER_REFRESH_TOKEN;

    private final PrivateCustomerEntity testCustomer = new PrivateCustomerEntity(
            null,
            "Jenny",
            "Doe",
            null,
            CustomerType.PRIVATE,
            "Johnny Street 1",
            12345,
            "Johnny City",
            "076-250 45 23",
            "jenny.doe@aol.com",
            "password",
            null);

    private final EmployeeEntity testEmployee = new EmployeeEntity(
            null,
            "Clean",
            "Cleanerson",
            null,
            CLEANER,
            "CleanCeanerson@Aol.se",
            "password",
            null
           );

    @PostConstruct
    public void getKeycloakData() {


        try {
/*
            // GET ADMIN TOKENENTITY
            KeycloakTokenEntity adminTokenEntity = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD);
            ADMIN_TOKEN = adminTokenEntity.getAccess_token();
            System.out.println("ADMIN TOKEN: " + adminTokenEntity);

            // LOGIN USER / GET USER TOKENENTITY
            KeycloakTokenEntity userTokenEntity = loginKeycloak(TEST_CUSTOMER_USERNAME, TEST_CUSTOMER_PASSWORD);
            USER_TOKEN = userTokenEntity.getAccess_token();
            USER_REFRESH_TOKEN = userTokenEntity.getRefresh_token();
            System.out.println("USER TOKEN: " + userTokenEntity);

            // GET A LIST OF USERS
            List<KeycloakUserEntity> users = getKeycloakUserEntities(ADMIN_TOKEN);
            System.out.println("USERS: " + users.toString());

            //GET A LIST OF ROLES
            List<KeycloakRoleEntity> roles = getKeycloakRoleEntities(ADMIN_TOKEN);
            System.out.println("ROLES: " + roles.toString());

            // CREATE A NEW CUSTOMER
            int createNewCustomerStatus = createNewCustomer(ADMIN_TOKEN, testCustomer).value();
            System.out.println("CREATE NEW CUSTOMER: " + createNewCustomerStatus);

            // CREATE A NEW EMPLOYEE
            int createNewEmployeeStatus = createNewEmployee(ADMIN_TOKEN, testEmployee).value();
            System.out.println("CREATE NEW EMPLOYEE: " + createNewEmployeeStatus);

            // ASSIGN A ROLE TO CUSTOMER
            int assignRoleToCustomer = assignRoleToCustomer(ADMIN_TOKEN, TEST_CUSTOMER_ID).value();
            System.out.println("ASSIGN ROLE TO CUSTOMER: " + assignRoleToCustomer);

            // ASSIGN ROLE TO EMPLOYEE
            int assignRoleToEmployee = assignRoleToEmployee(ADMIN_TOKEN, CLEANER, TEST_EMPLOYEE_ID).value();
            System.out.println("ASSIGN ROLE TO EMPLOYEE: " + assignRoleToEmployee);

            // CHANGE PASSWORD ON USER
            int changePasswordUser = changePasswordUser(ADMIN_TOKEN, TEST_CUSTOMER_ID, "nytt").value();
            System.out.println("CHANGE PASSWORD ON USER: " + changePasswordUser);

            // DELETE USER
            // WARNING!! NEED TO CREATE USER IN ADMIN UI AND PASTE ID HERE TO TEST.
            int deleteUser = deleteUser(ADMIN_TOKEN, "INSERT USER ID").value();
            System.out.println("DELETED USER: " + deleteUser);

            // LOGOUT USER
            int logoutUser = logoutKeycloak(USER_REFRESH_TOKEN).value();
            System.out.println("LOGOUT: " + logoutUser);

            // UPDATE CUSTOMER
            int updateCustomer = updateCustomer(ADMIN_TOKEN,"INSERT CUSTOMER ID", testCustomer).value();
            System.out.println("UPDATE CUSTOMER: " + updateCustomer);

            // UPDATE EMPLOYEE
            int updateEmployee = updateEmployee(ADMIN_TOKEN,"INSERT EMPLOYEE ID", testEmployee).value();
            System.out.println("UPDATE EMPLOYEE: " + updateEmployee);
*/

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //############################## HELPER METHODS #####################################

    public PrivateCustomerEntity addCustomerKeycloak(PrivateCustomerEntity customer) throws RuntimeException{
        String adminToken = getAdminTokenEntity(ADMIN_USERNAME, ADMIN_PASSWORD).getAccess_token();
        try{
           createNewCustomer(adminToken, customer).is2xxSuccessful();
           List<KeycloakUserEntity> addedUser = getKeycloakUserEntities(adminToken)
                   .stream()
                   .filter(entity -> entity.getUsername().equalsIgnoreCase(customer.getEmailAddress()))
                   .toList();
           if(addedUser.isEmpty()) {
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

    //############################# API CALLS ###########################################
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
            return response.getStatusCode(); // 201 IS SUCCESS
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // CREATE A NEW CUSTOMER IN THE KEYCLOAK DB
    public HttpStatusCode createNewEmployee(String adminToken, EmployeeEntity employee) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + adminToken);
            Credentials[] credArr = {
                    new Credentials("password",
                            employee.getPassword(),
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
    public HttpStatusCode assignRoleToEmployee(String adminToken,Role role, String employeeId)
            throws IllegalArgumentException{
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

    public HttpStatusCode updateCustomer(String adminToken, String customerId, PrivateCustomerEntity customer) {
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
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + customerId,
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

    public HttpStatusCode updateEmployee(String adminToken, String employeeId, EmployeeEntity employee) {
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
                    "http://localhost:8080/admin/realms/" + REALM + "/users/" + employeeId,
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

    private KeycloakRoleAssignmentEntity determineRole(Role role)
            throws IllegalArgumentException {
        return switch (role) {
            case ADMIN -> new KeycloakRoleAssignmentEntity(ROLE_ADMIN_ID, "client_admin");
            case CLEANER -> new KeycloakRoleAssignmentEntity(ROLE_CLEANER_ID, "client_cleaner");
            case CUSTOMER -> throw new IllegalArgumentException("Invalid role");
        };
    }

}
