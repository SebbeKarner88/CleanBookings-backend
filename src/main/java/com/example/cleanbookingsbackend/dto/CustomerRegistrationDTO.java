package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.CustomerType;


public record CustomerRegistrationDTO(String firstName,
                                      String lastName,
                                      CustomerType customerType,
                                      String streetAddress,
                                      Integer postalCode,
                                      String city,
                                      String phoneNumber,
                                      String emailAddress,
                                      String password) {
}
