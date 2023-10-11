package com.example.cleanbookingsbackend.model;

import com.example.cleanbookingsbackend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @SequenceGenerator(
            name = "bill_seq",
            sequenceName = "bill_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "bill_seq"
    )
    @Column(name = "id", columnDefinition = "numeric")
    private Integer id;

    @Column(name = "issue_date", columnDefinition = "varchar")
    @Temporal(TemporalType.DATE)
    private Date issueDate;

    @Column(name = "due_date", columnDefinition = "varchar")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @OneToOne
    @JoinColumn(name = "job_id", foreignKey = @ForeignKey(name = "fk_job_id"))
    private JobEntity job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "text")
    private PaymentStatus status;

    @Column(name = "price", columnDefinition = "numeric")
    private Double price;

    @PrePersist
    public void setDueDate() {
        if (issueDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(issueDate);
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            dueDate = calendar.getTime();
        }
    }
}
