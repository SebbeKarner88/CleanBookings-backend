package com.example.cleanbookingsbackend.DTO;

import com.example.cleanbookingsbackend.ENUM.CustomerType;

public record CustomerResponseDTO(String firstName,
                                  String lastName,
                                  CustomerType customerType,
                                  String streetAddress,
                                  Integer postalCode,
                                  String city,
                                  Integer phoneNumber,
                                  String emailAddress) {
}
