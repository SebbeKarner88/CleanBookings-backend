package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/api/v1/customer")
public class CustomerController {

    private final CustomerService customerService;


}
