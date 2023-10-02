package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.exception.UsernameIsTakenException;
import com.example.cleanbookingsbackend.exception.ValidationException;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private CustomerService customerService;

    @Test
    void testCreateCustomerWithValidData() throws ValidationException, UsernameIsTakenException, RuntimeException {
        // Given
        CustomerRegistrationDTO request = new CustomerRegistrationDTO(
                "Jan",
                "Andersson",
                CustomerType.PRIVATE,
                "Jan Street 1",
                12345,
                "Jan City",
                "076-250 90 80",
                "jan.andersson@AOL.com",
                "hashedPassword");

        //When
        when(customerRepository.existsByEmailAddress(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        CustomerResponseDTO response = customerService.create(request);

        // Then
        assertNotNull(response);
        assertInstanceOf(CustomerResponseDTO.class, response);
        assertEquals(response.emailAddress(), "jan.andersson@AOL.com");
    }

    @Test
    void testCreateCustomerWithInvalidData() {
        // Given
        CustomerRegistrationDTO request = new CustomerRegistrationDTO(
                "",
                "",
                CustomerType.PRIVATE,
                "Jan Street 1",
                12345,
                "Jan City",
                "076-250 90 80",
                "",
                passwordEncoder.encode("password"));
        // Then
        assertThrows(ValidationException.class, () -> customerService.create(request));
    }

    @Test
    void testCreateCustomerWithTakenUsername() {
        // Given
        CustomerRegistrationDTO request = new CustomerRegistrationDTO(
                "Jan",
                "Andersson",
                CustomerType.PRIVATE,
                "Jan Street 1",
                12345,
                "Jan City",
                "076-250 90 80",
                "jan.andersson@AOL.com",
                "hashedPassword");

        // When
        when(customerRepository.existsByEmailAddress(anyString())).thenReturn(true);

        // Then
        assertThrows(UsernameIsTakenException.class, () -> customerService.create(request));
    }
}