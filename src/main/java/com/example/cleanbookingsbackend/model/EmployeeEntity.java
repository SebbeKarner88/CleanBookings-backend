package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(name = "username_employee_unique", columnNames = "email_address"))
public class EmployeeEntity {

    @Id
    @Column(name = "id", columnDefinition = "text")
    private String id;

    @Column(name = "first_name", columnDefinition = "text")
    private String firstName;
    @Column(name = "last_name", columnDefinition = "text")
    private String lastName;
    @Column(name = "phone_number", columnDefinition = "text", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "email_address", columnDefinition = "text")
    private String emailAddress;
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @ManyToMany(mappedBy = "employee")
    private List<JobEntity> jobs;
}
