package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity
@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "private_customer", uniqueConstraints =@UniqueConstraint(name = "username_private_customer_unique", columnNames = "email_address"))
public class PrivateCustomerEntity {

    @Id
    @Column(name = "id", columnDefinition = "text")
    private String id;

    @Column(name = "first_name", columnDefinition = "text")
    private String firstName;

    @Column(name = "last_name", columnDefinition = "text")
    private String lastName;

    @Column(name = "social_security_number", columnDefinition = "text")
    private final String personNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "street_address", columnDefinition = "text")
    private String streetAddress;

    @Column(name = "postal_code", columnDefinition = "varchar(5)")
    private Integer postalCode;

    @Column(name = "city", columnDefinition = "text")
    private String city;

    @Column(name = "phone_number", columnDefinition = "text")
    private String phoneNumber;

    @Column(name = "email_address", columnDefinition = "text")
    private String emailAddress;

    @OneToMany(mappedBy = "customer")
    private List<JobEntity> jobs;

    public PrivateCustomerEntity(String personNumber) {
        this.personNumber = personNumber;
    }
}