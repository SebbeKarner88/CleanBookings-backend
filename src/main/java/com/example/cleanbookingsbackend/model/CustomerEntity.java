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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(name = "username_customer_unique", columnNames = "email_address"))
public class CustomerEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    @Column(name = "id", columnDefinition = "text")
    private String id;

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
    @Column(name = "phone_number", columnDefinition = "text")
    private String phoneNumber;

    @Column(name = "email_address", columnDefinition = "text")
    private String emailAddress;
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @OneToMany(mappedBy = "customer")
    private List<JobEntity> jobs;

}
