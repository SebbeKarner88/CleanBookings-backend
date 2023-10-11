package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {
    Optional<EmployeeEntity> findByEmailAddress(String email);
    boolean existsByEmailAddress(String email);
}