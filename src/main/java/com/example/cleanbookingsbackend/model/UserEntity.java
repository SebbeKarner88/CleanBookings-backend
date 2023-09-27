package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.ENUM.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_user", uniqueConstraints = @UniqueConstraint(name = "username_unique", columnNames = "email_address"))
public class UserEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String id;

    @Column(name = "first_name", columnDefinition = "text")
    private String firstName;
    @Column(name = "last_name", columnDefinition = "text")
    private String lastName;
    @Column(name = "phone_number", columnDefinition = "int")
    private Integer phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "email_address", columnDefinition = "text")
    private String emailAddress;
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;
}
