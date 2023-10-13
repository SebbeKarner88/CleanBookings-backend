package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
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

    public static CustomerDataResponse fromEntity(PrivateCustomerEntity privateCustomerEntity) {
        CustomerDataResponse response = new CustomerDataResponse();
        response.setId(privateCustomerEntity.getId());
        response.setFirstName(privateCustomerEntity.getFirstName());
        response.setLastName(privateCustomerEntity.getLastName());
        response.setCustomerType(privateCustomerEntity.getCustomerType());
        response.setStreetAddress(privateCustomerEntity.getStreetAddress());
        response.setPostalCode(privateCustomerEntity.getPostalCode());
        response.setCity(privateCustomerEntity.getCity());
        response.setPhoneNumber(privateCustomerEntity.getPhoneNumber());
        response.setEmailAddress(privateCustomerEntity.getEmailAddress());
        return response;
    }
}
