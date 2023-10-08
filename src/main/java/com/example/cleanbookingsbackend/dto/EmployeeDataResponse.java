package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.model.EmployeeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeDataResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private String emailAddress;

    public static EmployeeDataResponse fromEntity(EmployeeEntity employee) {
        EmployeeDataResponse response = new EmployeeDataResponse();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setPhoneNumber(employee.getPhoneNumber());
        response.setRole(employee.getRole().toString());
        response.setEmailAddress(employee.getEmailAddress());
        return response;
    }
}
