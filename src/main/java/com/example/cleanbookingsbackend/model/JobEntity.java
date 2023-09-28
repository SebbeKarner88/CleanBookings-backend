
package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job")
public class JobEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    @Column(name = "id", columnDefinition = "text")
    private String id;

    @Column(name = "booked_date", columnDefinition = "varchar")
    private Date bookedDate;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_employee",
            joinColumns = @JoinColumn(name = "employee_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "job_id",
                    referencedColumnName = "id")
    )
    private List<EmployeeEntity> employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @OneToOne(mappedBy = "job")
    private PaymentEntity payment;

}

