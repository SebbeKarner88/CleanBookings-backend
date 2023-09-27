package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.ENUM.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    @Column(name = "id", columnDefinition = "text")
    private String id;

    @OneToOne
    @JoinColumn(name = "job_id", foreignKey = @ForeignKey(name = "fk_job_id"))
    private JobEntity job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "text")
    private PaymentStatus status;

    @Column(name = "price", columnDefinition = "numeric")
    private Double price;

}
