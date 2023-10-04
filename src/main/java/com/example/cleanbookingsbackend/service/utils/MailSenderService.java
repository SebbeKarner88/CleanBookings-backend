package com.example.cleanbookingsbackend.service.utils;

import com.example.cleanbookingsbackend.dto.JobApproveRequest;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;

    public void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(requestedJob.getCustomer().getEmailAddress());
        msg.setSubject("Din bokningsbekräftelse");
        msg.setText("Hej " + requestedJob.getCustomer().getFirstName() + "! Din bokning av " + requestedJob.getType() + " på " + requestedJob.getBookedDate() + " har bekräftats.\n\n" +
                "StädaFint AB");
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
        msg.setText("Hej " + requestedCancel.getCustomer().getFirstName() + "! Er bokning av " + requestedCancel.getType() + " på " + requestedCancel.getBookedDate() + " är nu avbokad. \n\n" +
                "Varmt välkommen åter!\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationOnAssignedJob(EmployeeEntity cleaner, JobEntity job) {
        // Todo: Should we send a mail to the customer aswell??
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(cleaner.getEmailAddress());
        msg.setSubject("Nytt städjobb för " + cleaner.getFirstName() + "!");
        msg.setText("Hej " + cleaner.getFirstName() + "! \n\nDu har fått ett nytt städjobb inbokat: "
                + job.getBookedDate() + ", " + job.getType() + "Meddelande: " + job.getMessage() + "\n\nFör mer information logga in på CleanBookings.");
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
                "/n/nVi hoppas att ni är nöjd med arbetet, navigera till CleanBookings för att godkänna/underkänna städningen./n/n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationApprovedJob(JobEntity job) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(job.getCustomer().getEmailAddress());
        msg.setSubject("Tack för förtroendet!");
        msg.setText("Hej " + job.getCustomer().getFirstName() + "! \n\nNi har nu godkänt städningen vi har utfört hos er." +
                "\n\nVi tackar så mycket för ert förtroende och hoppas att ni väljer oss igen nästa gång ni behöver hjälp med städ. " +
                "\n\nBetalning kommer ske enligt överenskommelse vid bokning.\n\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationFailedJob(JobEntity job) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(job.getCustomer().getEmailAddress());
        msg.setSubject("Tack för din feedback!");
        msg.setText("Hej " + job.getCustomer().getFirstName() + "! \n\nHär kommer en bekräftelse på att vi mottagit ert meddelande." +
                "\n\nVi ber om ursäkt att vårat jobb inte levde upp till era förväntningar. \nVi har meddelat våra städare och åtgärd kommer att utföras inom kort. \n\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    public void sendEmailConfirmationReissuedJob(JobEntity job) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setSubject("Backjobb på jobb" + job.getType() + " hos " + job.getCustomer().getFirstName() + " " + job.getCustomer().getLastName() + ".");
        msg.setText("Kärna Kollega! \n\nVi har tyvärr fått ett backjobb på " + job.getType() + " hos " + job.getCustomer().getFirstName() + " " + job.getCustomer().getLastName() + ".\n\n" +
                "Vi ber dig därför att så snart som möjligt åtgärda de punkter kunden kommenterat i svaret. Tack!\n\n" + job.getMessage() + "\n\n Städafint AB.");
        try {
            for (EmployeeEntity cleaner : job.getEmployee()) {
                msg.setTo(cleaner.getEmailAddress());
                mailSender.send(msg);
            }
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }
}
