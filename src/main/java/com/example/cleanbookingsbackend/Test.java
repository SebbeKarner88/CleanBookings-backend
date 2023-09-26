package com.example.cleanbookingsbackend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;


@Entity
@Table(name = "test")
public class Test {

    @Id
    UUID id;
    String test;
}
