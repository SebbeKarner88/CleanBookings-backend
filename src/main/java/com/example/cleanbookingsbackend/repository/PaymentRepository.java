package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.enums.PaymentStatus;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository <PaymentEntity, String> {
    List<PaymentEntity> findByDueDateBeforeAndStatus(Date date, PaymentStatus status);
}
