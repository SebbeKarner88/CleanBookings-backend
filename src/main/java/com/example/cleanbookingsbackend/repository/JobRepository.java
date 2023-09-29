package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository <JobEntity, String> {
    Optional<JobEntity> findJobEntityByBookedDateAndType(Date date, JobType type);
}