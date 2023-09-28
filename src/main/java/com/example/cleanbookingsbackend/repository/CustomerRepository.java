package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository <CustomerEntity, String> {

    boolean existsByEmailAddress(String email);
}
