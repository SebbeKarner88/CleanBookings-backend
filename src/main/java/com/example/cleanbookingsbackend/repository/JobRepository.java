package com.example.cleanbookingsbackend.repository;

import com.example.cleanbookingsbackend.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository <JobEntity, String> {


}
