package com.example.cleanbookingsbackend.keycloak;

import com.example.cleanbookingsbackend.keycloak.models.KeycloakUserEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
            System.out.println(users.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }




    // GET ALL USERS FROM KEYCLOAK REALM AND RETURNING A LIST WITH KEYCLOAKUSERENTITY
    public List<KeycloakUserEntity> getUsers() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA" +
                    "6ICIwWkloYmFtY0RVTVpJX0lDbXVSSXg3aDNvOGl5THd3RkNYcmI3NWIydFBJIn0.eyJleHAiOjE2OTgxMzczNTIsImlhdCI6" +
                    "MTY5ODEzNjQ1MiwianRpIjoiOTMxYTdiM2ItNGNlNi00NDVmLWFiYTgtNjc4MzVlZDNmMTFlIiwiaXNzIjoiaHR0cDovL2xvY" +
                    "2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIzNWIzMTgxZi1lOWQzLTRiYzMtYjEwYS05NDIzMmFmYWI5NjIiLC" +
                    "J0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJzZXNzaW9uX3N0YXRlIjoiZWI4MDE0ZWUtMGYwMC00Y2JmLTgxODI" +
                    "tOWI4OGI2NWJiNTA4IiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6ImViODAxNGVlLTBmMDAtNGNi" +
                    "Zi04MTgyLTliODhiNjViYjUwOCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4if" +
                    "Q.TVstitngINTivgtYr3kZ6dz474fX-ZrAWeRvPlt3iyZHssDF4Ojbv2eVZiz0vsKjPyaiSZjFxhnnjL_PkdGiEeKsr8fkVJ" +
                    "R3kJbgweI0Vn06kz9ppSWZG24bqcveOQATiyWrVgu5FyfknOL2Gs8gtyHhDnVHKpteh_ZLeLv7jCSVXxwMzBQThYjdDQW-R8" +
                    "Ef24uO3oe3UEzLT2ZFcUu8wxsoNU0lz59WCHdN86nEJDptXxavHXtyDuew7s8A7IjeV-xBqEw92WM5Gh4v7p0lp8PqKAcXDHi" +
                    "YfCcioLOKDPqCBRqn4c1eAlDzzZ6yTqv0A-zu9GISYBAqJ39SFFuhbA");

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



}
