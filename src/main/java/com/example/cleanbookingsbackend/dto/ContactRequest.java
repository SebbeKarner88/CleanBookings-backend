package com.example.cleanbookingsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContactRequest {
    private String name;
    private String email;
    private String subject;
    private String message;
}