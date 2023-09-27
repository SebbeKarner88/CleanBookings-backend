package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository <PaymentEntity, String> {

}
