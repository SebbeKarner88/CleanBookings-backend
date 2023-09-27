package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/api/v1/user")
public class EmployeeController {

    private final EmployeeService userService;


}
