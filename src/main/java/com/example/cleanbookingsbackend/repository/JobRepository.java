package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository <JobEntity, String> {
    Optional<JobEntity> findByBookedDateAndType(Date date, JobType type);

    List<JobEntity> findByCustomerAndStatusNot(CustomerEntity customer, JobStatus status);

    @Query("SELECT j FROM JobEntity j WHERE j.customer.id = :customerId AND j.status <> 'CLOSED'")
    List<JobEntity> findJobsByCustomerIdAndStatusNotClosed(@Param("customerId") String customerId);

    List<JobEntity> findByCustomer_Id(String customerId);

    @Query("SELECT j FROM JobEntity j WHERE j.customer.id = :customerId AND j.status = 'CLOSED'")
    List<JobEntity> findJobsByCustomerIdAndStatusClosed(@Param("customerId") String customerId);

    @Query("SELECT j FROM JobEntity j WHERE j.customer.id = :customerId AND j.status = :status")
    List<JobEntity> findByCustomerIdAndStatus(String customerId, JobStatus status);

}
