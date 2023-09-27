package com.example.cleanbookingsbackend.Models;

import com.example.cleanbookingsbackend.ENUM.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(name = "username_unique", columnNames = "email_address"))
public class CustomerEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @Column(name = "first_name", columnDefinition = "text")
    private String firstName;
    @Column(name = "last_name", columnDefinition = "text")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "street_address", columnDefinition = "text")
    private String streetAddress;
    @Column(name = "postal_code", columnDefinition = "varchar(5)")
    private Integer postalCode;
    @Column(name = "city", columnDefinition = "text")
    private String city;
    @Column(name = "phone_number", columnDefinition = "int")
    private Integer phoneNumber;

    @Column(name = "email_address", columnDefinition = "text")
    private String emailAddress;
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;
}
