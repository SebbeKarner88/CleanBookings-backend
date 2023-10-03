package com.example.cleanbookingsbackend.service.utils;

import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;

    public void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(requestedJob.getCustomer().getEmailAddress());
        msg.setSubject("Din bokningsbekräftelse");
        msg.setText("Hej " + requestedJob.getCustomer().getFirstName() + "! Din bokning av " + requestedJob.getType() + " på " + requestedJob.getBookedDate() + " har bekräftats.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationCanceledJob(JobEntity requestedCancel) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(requestedCancel.getCustomer().getEmailAddress());
        msg.setSubject("Avbokad städning");
        msg.setText("Hej " + requestedCancel.getCustomer().getFirstName() + "! Er bokning av " + requestedCancel.getType() + " på " + requestedCancel.getBookedDate() + " är nu avbokad. /n" +
                "Varmt välkommen åter!");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationOnAssignedJob(EmployeeEntity cleaner, JobEntity job) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(cleaner.getEmailAddress());
        msg.setSubject("Nytt städjobb för " + cleaner.getFirstName() + "!");
        msg.setText("Hej " + cleaner.getFirstName() + "! /n/nDu har fått ett nytt städjobb inbokat: "
                + job.getBookedDate() + ", " + job.getType() + "Meddelande: " + job.getMessage() + "/n/nFör mer information logga in på CleanBookings.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationExecutedJob(JobEntity job, EmployeeEntity cleaner) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(job.getCustomer().getEmailAddress());
        msg.setSubject("Städning Slutförd!");
        msg.setText("Hej " + job.getCustomer().getFirstName() + "! /n/nNu har " + cleaner.getFirstName() + " slutfört sin städning hos dig." +
                "/n/nVi hoppas att ni är nöjd med arbetet, navigera till CleanBookings för att godkänna/underkänna städningen.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }
}
