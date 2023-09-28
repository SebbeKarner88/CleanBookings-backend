package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.CustomerType;


public record CustomerRegistrationDTO(String firstName,
                                      String lastName,
                                      CustomerType customerType,
                                      String streetAddress,
                                      Integer postalCode,
                                      String city,
                                      Integer phoneNumber,
                                      String emailAddress,
                                      String password) {
}
