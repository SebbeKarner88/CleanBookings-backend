package com.example.cleanbookingsbackend.service.DataInitialization;

import com.example.cleanbookingsbackend.enums.*;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InitDataService {
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder encoder;

    @PostConstruct
    public void initializeData() {

        // ##### Customers #####

        PrivateCustomerEntity customer1 = new PrivateCustomerEntity(
                null,
                "Jane",
                "Doe",
                encoder.encode("880325-2456"),
                CustomerType.PRIVATE,
                "Jane Street 1",
                12345,
                "Jane City",
                "076-250 90 80",
                "jane.doe@janecity.com",
                encoder.encode("password"),
                null);

        PrivateCustomerEntity customer2 = new PrivateCustomerEntity(
                null,
                "Johnny",
                "Doe",
                encoder.encode("730623-0145"),
                CustomerType.PRIVATE,
                "Jhonny Street 1",
                12345,
                "Jhonny City",
                "076-250 45 23",
                "jhonny.doe@aol.com",
                encoder.encode("password"),
                null);

        PrivateCustomerEntity customer3 = new PrivateCustomerEntity(
                null,
                "Anders",
                "Svensson",
                encoder.encode("450919-0945"),
                CustomerType.PRIVATE,
                "Gatgatan 1",
                12321,
                "Stadstaden",
                "076-253 45 43",
                "anders.Svensson@google.com",
                encoder.encode("password"),
                null);

        PrivateCustomerEntity customer4 = new PrivateCustomerEntity(
                null,
                "Maj-Britt",
                "Hemmafrusson",
                encoder.encode("901224-0165"),
                CustomerType.PRIVATE,
                "Adress 2",
                11111,
                "Staden",
                "070777777",
                "majsan@hotmail.se",
                encoder.encode("password"),
                null);

        customerRepository.saveAll(List.of(customer1, customer2, customer3, customer4));

        // ##### Cleaner/admin #####

        EmployeeEntity cleaner1 = new EmployeeEntity(
                null,
                "Klas",
                "Cleanerson",
                "073-9 453 843",
                Role.CLEANER,
                "Klas@CleanBookings.com",
                encoder.encode("password"),
                null
        );
        EmployeeEntity cleaner2 = new EmployeeEntity(
                null,
                "Anita",
                "Städgren",
                "073-9 351 733",
                Role.CLEANER,
                "Anita@CleanBookings.com",
                encoder.encode("password"),
                null

        );
        EmployeeEntity cleaner3 = new EmployeeEntity(
                null,
                "Stig",
                "Städarson",
                "073-9 467 843",
                Role.CLEANER,
                "Stig@CleanBookings.com",
                encoder.encode("password"),
                null
        );
        EmployeeEntity cleaner4 = new EmployeeEntity(
                null,
                "Ebba",
                "Sopasson",
                "073-9 031 733",
                Role.CLEANER,
                "Ebba@CleanBookings.com",
                encoder.encode("password"),
                null

        );
        EmployeeEntity cleaner5 = new EmployeeEntity(
                null,
                "Svetlana",
                "Putszki",
                "072-9 453 843",
                Role.CLEANER,
                "Sveta@CleanBookings.com",
                encoder.encode("password"),
                null
        );
        EmployeeEntity cleaner6 = new EmployeeEntity(
                null,
                "Våt-Torkas",
                "Kvastdottir",
                "073-9 351 453",
                Role.CLEANER,
                "Torkas@CleanBookings.com",
                encoder.encode("password"),
                null

        );
        EmployeeEntity admin1 = new EmployeeEntity(
                null,
                "Admin",
                "Administrator",
                "074-9 433 243",
                Role.ADMIN,
                "Admin1@CleanBookings.com",
                encoder.encode("password"),
                null
        );

        employeeRepository.saveAll(List.of(cleaner1, cleaner2, cleaner3, cleaner4, cleaner5, cleaner6, admin1));

        // ##### Jobs #####

        JobEntity job1Customer1Cleaner1 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 5),
                customer1,
                null,
                JobType.BASIC_CLEANING,
                "I want you to clean my car aswell",
                JobStatus.OPEN,
                null
        );
        JobEntity job2Customer2Cleaner1And2 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7),
                customer2,
                List.of(cleaner1, cleaner2),
                JobType.WINDOW_CLEANING,
                "I need you to be done before 16:00",
                JobStatus.ASSIGNED,
                null
        );
        JobEntity job3Customer2Cleaner2 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7),
                customer2,
                List.of(cleaner2),
                JobType.TOPP_CLEANING,
                "I WANT IT SQUEAKY CLEAN!",
                JobStatus.APPROVED,
                null
        );
        JobEntity job4Customer1Cleaner1And2 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3),
                customer1,
                List.of(cleaner1, cleaner2),
                JobType.DIAMOND_CLEANING,
                "I will never approve this cleaning!",
                JobStatus.NOT_APPROVED,
                null
        );
        JobEntity job5Customer2Cleaner1 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 2),
                customer2,
                List.of(cleaner1),
                JobType.BASIC_CLEANING,
                "I never pay my bills on time!",
                JobStatus.APPROVED,
                null
        );
        JobEntity job6Customer2Cleaner2 = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7),
                customer2,
                List.of(cleaner2),
                JobType.TOPP_CLEANING,
                "Waiting for my approval are you?!",
                JobStatus.WAITING_FOR_APPROVAL,
                null
        );

        jobRepository.saveAll(List.of(job1Customer1Cleaner1,
                job2Customer2Cleaner1And2,
                job3Customer2Cleaner2,
                job4Customer1Cleaner1And2,
                job5Customer2Cleaner1,
                job6Customer2Cleaner2
        ));

        // ##### Payment #####

        PaymentEntity paymentJob3 = new PaymentEntity(
                null,
                new Date(System.currentTimeMillis()),
                null,
                job3Customer2Cleaner2,
                PaymentStatus.INVOICED,
                795.50
        );

        PaymentEntity paymentJob5 = new PaymentEntity(
                null,
                new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 34),
                null,
                job5Customer2Cleaner1,
                PaymentStatus.OVERDUE,
                1995.0
        );
        paymentRepository.saveAll(List.of(paymentJob3, paymentJob5));
    }
}
