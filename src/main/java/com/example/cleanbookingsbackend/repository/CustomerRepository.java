package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository <PrivateCustomerEntity, String> {

    boolean existsByEmailAddress(String email);
    Optional<PrivateCustomerEntity> findByEmailAddress(String email);

    Optional<PrivateCustomerEntity> findById(String id);
}
