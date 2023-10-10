package com.example.cleanbookingsbackend.service.utils;

import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final static SimpleMailMessage msg = new SimpleMailMessage();
    private final static String CLEAN_BOOKINGS = "order.cleanbookings@gmail.com";
    private final static String EMAIL_NOT_SENT = "Email couldn't be sent: ";

    public void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(getCustomerEmailAdress(requestedJob));
        msg.setSubject("Din bokningsförfrågan.");
        msg.setText("Hej " + getCustomerName(requestedJob) + "! Er bokning av " + requestedJob.getType() + " den "
                + getFormattedDate(requestedJob) + " har tagits emot av oss.\n\nNi kommer att få en bekräftelse på " +
                "bokningen när vi bokat in en städare på ert jobb.\n\n" + "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationCanceledJob(JobEntity requestedCancel) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(getCustomerEmailAdress(requestedCancel));
        msg.setSubject("Avbokad städning");
        msg.setText("Hej " + getCustomerName(requestedCancel) + "! Er bokning av " + requestedCancel.getType() +
                " den " + getFormattedDate(requestedCancel) + " är nu avbokad. \n\n" +
                "Varmt välkommen åter!\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationOnAssignedJob(EmployeeEntity cleaner, JobEntity job) {
        // CLEANER
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(cleaner.getEmailAddress());
        msg.setSubject("Nytt städjobb för " + cleaner.getFirstName() + "!");
        msg.setText("Hej " + cleaner.getFirstName() + "! \n\nDu har fått ett nytt städjobb inbokat: "
                + getFormattedDate(job) + ", " + job.getType() + "Meddelande: " + job.getMessage() +
                "\n\nFör mer information logga in på CleanBookings.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
        // CUSTOMER
        msg.setTo(job.getCustomer().getEmailAddress());
        msg.setSubject("Bokningsbekräftelse på " + job.getType() + ".");
        msg.setText("Hej " + job.getCustomer().getFirstName() + "! \n\nHär kommer en bekräftelse på att er bokningsförfrågan nu har tilldelats en städare och är inbokat. \n\n" +
                cleaner.getFirstName() + " " + cleaner.getLastName() + " från Städafint AB kommer att utföra er städning den " + job.getBookedDate());
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationExecutedJob(JobEntity job, EmployeeEntity cleaner) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(getCustomerEmailAdress(job));
        msg.setSubject("Städning Slutförd!");
        msg.setText("Hej " + getCustomerName(job) + "! /n/nNu har " + cleaner.getFirstName() + " slutfört sin städning hos dig." +
                "/n/nVi hoppas att ni är nöjd med arbetet, navigera till CleanBookings för att godkänna/underkänna städningen./n/n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationApprovedJob(JobEntity job) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(getCustomerEmailAdress(job));
        msg.setSubject("Tack för förtroendet!");
        msg.setText("Hej " + getCustomerName(job) + "! \n\nNi har nu godkänt städningen vi har utfört hos er." +
                "\n\nVi tackar så mycket för ert förtroende och hoppas att ni väljer oss igen nästa gång ni behöver hjälp med städ. " +
                "\n\nBetalning kommer ske enligt överenskommelse vid bokning.\n\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationFailedJob(JobEntity job) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setTo(getCustomerEmailAdress(job));
        msg.setSubject("Tack för din feedback!");
        msg.setText("Hej " + getCustomerName(job) + "! \n\nHär kommer en bekräftelse på att vi mottagit ert meddelande." +
                "\n\nVi ber om ursäkt att vårat jobb inte levde upp till era förväntningar. \nVi har meddelat våra städare" +
                " och åtgärd kommer att utföras inom kort. Tack för ert tålamod!\n\n" +
                "StädaFint AB");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationReissuedJob(JobEntity job) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setSubject("Backjobb på jobb" + job.getType() + " hos " + getCustomerName(job) + " " + job.getCustomer().getLastName() + ".");
        msg.setText("Kärna Kollega! \n\nVi har tyvärr fått ett backjobb på " + job.getType() + " hos " + getCustomerName(job) + " " + job.getCustomer().getLastName() + ".\n\n" +
                "Vi ber dig därför att så snart som möjligt åtgärda de punkter kunden kommenterat i svaret. Tack!\n\n" + job.getMessage() + "\n\n Städafint AB.");
        try {
            for (EmployeeEntity cleaner : job.getEmployee()) {
                msg.setTo(cleaner.getEmailAddress());
                mailSender.send(msg);
            }
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendInvoice(JobEntity job) {
        msg.setFrom(CLEAN_BOOKINGS);
        msg.setSubject("Faktura på utförd städning");
        msg.setText("!!!Fakturatext!!!");
        try {
            msg.setTo(getCustomerEmailAdress(job));
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    private static String getCustomerEmailAdress(JobEntity requestedJob) {
        return requestedJob.getCustomer().getEmailAddress();
    }

    private static String getCustomerName(JobEntity requestedCancel) {
        return requestedCancel.getCustomer().getFirstName();
    }

    private static String getFormattedDate(JobEntity requestedJob) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm").format(requestedJob.getBookedDate());
    }
}
