package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository <JobEntity, String> {
    Optional<JobEntity> findByBookedDateAndType(Date date, JobType type);

    List<JobEntity> findByCustomerAndStatusNot(CustomerEntity customer, JobStatus status);

    List<JobEntity> findByCustomer_Id(String customerId);
}
