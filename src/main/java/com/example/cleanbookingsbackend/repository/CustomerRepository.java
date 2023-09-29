package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository <CustomerEntity, String> {

    boolean existsByEmailAddress(String email);
    CustomerEntity findByEmailAddress(String email);

    Optional<CustomerEntity> findById(String id);
}
