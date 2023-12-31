
package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.enums.Timeslot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Enumerated(EnumType.STRING)
    @Column(name = "timeslot")
    private Timeslot timeslot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private PrivateCustomerEntity customer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_employee",
            joinColumns = @JoinColumn(name = "job_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id",
                    referencedColumnName = "id")
    )
    private List<EmployeeEntity> employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private JobType type;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @OneToOne(mappedBy = "job")
    private PaymentEntity payment;

    public JobEntity(PrivateCustomerEntity customer, Timeslot timeslot, JobType type, Date bookedDate, String message) {
        this.customer = customer;
        this.type = type;
        this.timeslot = timeslot;
        this.status = JobStatus.OPEN;
        this.bookedDate = bookedDate;
        this.message = message;
    }
}

