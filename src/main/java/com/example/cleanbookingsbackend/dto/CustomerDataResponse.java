package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerDataResponse {
    private String id;
    private String firstName;
    private String lastName;
    private CustomerType customerType;
    private String streetAddress;
    private Integer postalCode;
    private String city;
    private String phoneNumber;
    private String emailAddress;

    // Constructors, getters, and setters

    public static CustomerDataResponse fromEntity(CustomerEntity customerEntity) {
        CustomerDataResponse response = new CustomerDataResponse();
        response.setId(customerEntity.getId());
        response.setFirstName(customerEntity.getFirstName());
        response.setLastName(customerEntity.getLastName());
        response.setCustomerType(customerEntity.getCustomerType());
        response.setStreetAddress(customerEntity.getStreetAddress());
        response.setPostalCode(customerEntity.getPostalCode());
        response.setCity(customerEntity.getCity());
        response.setPhoneNumber(customerEntity.getPhoneNumber());
        response.setEmailAddress(customerEntity.getEmailAddress());
        return response;
    }
}
