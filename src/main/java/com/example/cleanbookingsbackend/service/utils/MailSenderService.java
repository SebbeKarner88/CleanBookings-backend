package com.example.cleanbookingsbackend.service.utils;

import com.example.cleanbookingsbackend.dto.ContactRequest;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

//    private final static SimpleMailMessage msg = new SimpleMailMessage();
    private final static String CLEAN_BOOKINGS = "order.cleanbookings@gmail.com";
    private final static String EMAIL_NOT_SENT = "Email couldn't be sent: ";


    public String translateJobType(String type) {
        return switch (type) {
            case "BASIC_CLEANING" -> "BASIC-städning";
            case "TOPP_CLEANING" -> "TOPP-städning";
            case "WINDOW_CLEANING" -> "Fönsterputs";
            case "DIAMOND_CLEANING" -> "DIAMANT-städning";
            default -> type; // Default case to handle unexpected types
        };
    }

    private void sendEmailWithTemplate(JobEntity jobEntity, String emailSubject, String templateName) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            String translatedJobType = translateJobType(String.valueOf(jobEntity.getType()));

            Context context = new Context();
            context.setVariable("customerName", getCustomerName(jobEntity));
            context.setVariable("jobType", translatedJobType); // Translated job type
            context.setVariable("bookingDateTime", getFormattedDateAndTime(jobEntity.getBookedDate()));

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(jobEntity));
            helper.setSubject(emailSubject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }


//    public void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//
//        try {
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
//
//            String translatedJobType = translateJobType(String.valueOf(requestedJob.getType()));
//
//            Context context = new Context();
//            context.setVariable("customerName", getCustomerName(requestedJob));
//            context.setVariable("jobType", translatedJobType); // Translated job type
//            context.setVariable("bookingDateTime", getFormattedDateAndTime(requestedJob.getBookedDate()));
//
//            String htmlContent = templateEngine.process("bookingConfirmation", context);
//
//            helper.setFrom(CLEAN_BOOKINGS);
//            helper.setTo(getCustomerEmailAdress(requestedJob));
//            helper.setSubject("Din bokningsförfrågan.");
//            helper.setText(htmlContent, true);
//
//            mailSender.send(mimeMessage);
//        } catch (MailException | MessagingException exception) {
//            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
//        }
//    }
//
//
//    public void sendEmailConfirmationCanceledJob(JobEntity requestedCancel) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//
//        try {
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
//
//            String translatedJobType = translateJobType(String.valueOf(requestedCancel.getType()));
//
//            Context context = new Context();
//            context.setVariable("customerName", getCustomerName(requestedCancel));
//            context.setVariable("jobType", translatedJobType); // Translated job type
//            context.setVariable("bookingDateTime", getFormattedDateAndTime(requestedCancel.getBookedDate()));
//
//            String htmlContent = templateEngine.process("cancelJobConfirmation", context);
//
//            helper.setFrom(CLEAN_BOOKINGS);
//            helper.setTo(getCustomerEmailAdress(requestedCancel));
//            helper.setSubject("Avbokad städning");
//            helper.setText(htmlContent, true);
//
//            mailSender.send(mimeMessage);
//        } catch (MailException | MessagingException exception) {
//            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
//        }
//    }

    public void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
        sendEmailWithTemplate(requestedJob, "Din bokningsförfrågan.", "bookingConfirmation");
    }

    public void sendEmailConfirmationCanceledJob(JobEntity requestedCancel) {
        sendEmailWithTemplate(requestedCancel, "Avbokad städning", "cancelJobConfirmation");
    }

    public void sendEmailConfirmationOnAssignedJob(EmployeeEntity cleaner, JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessageToCleaner = "Hej " + cleaner.getFirstName() + "! \n\nDu har fått ett nytt städjobb inbokat: "
                + getFormattedDateAndTime(job.getBookedDate()) + ", " + job.getType() + "Meddelande: " + job.getMessage() +
                "\n\nFör mer information logga in på CleanBookings.";
        String originalMessageToCustomer = "Hej " + job.getCustomer().getFirstName() + "! \n\nHär kommer en bekräftelse på att er bokningsförfrågan nu har tilldelats en städare och är inbokat. \n\n" +
                cleaner.getFirstName() + " " + cleaner.getLastName() + " från Städafint AB kommer att utföra er städning den " + job.getBookedDate();

        // CLEANER
        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(cleaner.getEmailAddress());
            helper.setSubject("Nytt städjobb för " + cleaner.getFirstName() + "!");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }

        // CUSTOMER
        try {
            helper.setTo(job.getCustomer().getEmailAddress());
            helper.setSubject("Bokningsbekräftelse på " + job.getType() + ".");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationExecutedJob(JobEntity job, EmployeeEntity cleaner) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Hej " + getCustomerName(job) + "! /n/nNu har " + cleaner.getFirstName() + " slutfört sin städning hos dig." +
                "/n/nVi hoppas att ni är nöjd med arbetet, navigera till CleanBookings för att godkänna/underkänna städningen./n/n" +
                "StädaFint AB";

        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(job));
            helper.setSubject("Städning Slutförd!");
            helper.setText("<h1>Ein schnippet!</h1>", true);

            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationApprovedJob(JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Hej " + getCustomerName(job) + "! \n\nNi har nu godkänt städningen vi har utfört hos er." +
                "\n\nVi tackar så mycket för ert förtroende och hoppas att ni väljer oss igen nästa gång ni behöver hjälp med städ. " +
                "\n\nBetalning kommer ske enligt överenskommelse vid bokning.\n\n" +
                "StädaFint AB";
        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(job));
            helper.setSubject("Tack för förtroendet!");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationFailedJob(JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Hej " + getCustomerName(job) + "! \n\nHär kommer en bekräftelse på att vi mottagit ert meddelande." +
                "\n\nVi ber om ursäkt att vårat jobb inte levde upp till era förväntningar. \nVi har meddelat våra städare" +
                " och åtgärd kommer att utföras inom kort. Tack för ert tålamod!\n\n" +
                "StädaFint AB";

        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(job));
            helper.setSubject("Tack för din feedback!");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationReissuedJob(JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Kärna Kollega! \n\nVi har tyvärr fått ett backjobb på " + job.getType() + " hos " + getCustomerName(job) + " " + job.getCustomer().getLastName() + ".\n\n" +
                "Vi ber dig därför att så snart som möjligt åtgärda de punkter kunden kommenterat i svaret. Tack!\n\n" + job.getMessage() + "\n\n Städafint AB.";

        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setSubject("Backjobb på jobb" + job.getType() + " hos " + getCustomerName(job) + " " + job.getCustomer().getLastName() + ".");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            for (EmployeeEntity cleaner : job.getEmployee()) {
                helper.setTo(cleaner.getEmailAddress());
                mailSender.send(mimeMessage);
            }
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendInvoice(JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Faktura StädaFint AB                              Fakturanummer: " + job.getPayment().getId() + "\n\n\n\n" +
                "Betalvillkor: 30 dagar netto                      Betalsätt: Faktura\n" +
                "Fakturadatum: " + getFormattedDate(job.getPayment().getIssueDate()) + "                          Förfallodag: " + getFormattedDate(job.getPayment().getDueDate()) + "\n\n" +
                "____________________________________________________________________\n" +
                job.getType() + "                Antal: 1                 Pris: " + job.getPayment().getPrice() + "kr ink. moms.\n" +
                "____________________________________________________________________\n" +
                "                                                         Total: " + job.getPayment().getPrice() + "kr ink. moms.\n\n\n\n" +
                "Vid betalning vänligen uppge fakturanummer som meddelande.\n\n" +
                "Tack så mycket för att ni anlitade oss, vi hoppas att vi snart ses igen!\n\n" +
                "Städafint AB";
        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(job));
            helper.setSubject("Faktura på utförd städning");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationOnPaidInvoice(JobEntity job) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Hej " + getCustomerName(job) + "! \n\nHär kommer en bekräftelse på att vi mottagit betalning på jobb: ." + job.getType() +
                "\n\nVi tackar så mycket för ert förtroende och vi hoppas att ni är nöjda med vårat arbete. På återseende!\n\n" +
                "StädaFint AB";

        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(getCustomerEmailAdress(job));
            helper.setSubject("Tack för din betalning");
            helper.setText("<h1>Ein schnippet!</h1>", true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    public void sendEmailConfirmationMessageReceived(ContactRequest request) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String originalMessage = "Hej " + request.getName() + "! " +
                "\n\nVi har mottagit ditt meddelande och återkommer så snart vi kan." +
                "\n\nStädaFint AB" +
                "\n\nÄmne:\n" + request.getSubject() +
                "\n\nMeddelande:\n" + request.getMessage();

        try {
            helper.setFrom(CLEAN_BOOKINGS);
            helper.setTo(request.getEmail());
            helper.setSubject("Tack för ditt meddelande");
            helper.setText(request.getHtmlSnippet(), true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException exception) {
            System.out.println(EMAIL_NOT_SENT + exception.getMessage());
        }
    }

    private static String getCustomerEmailAdress(JobEntity requestedJob) {
        return requestedJob.getCustomer().getEmailAddress();
    }

    private static String getCustomerName(JobEntity requestedCancel) {
        return requestedCancel.getCustomer().getFirstName();
    }

    private static String getFormattedDateAndTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm").format(date);
    }

    private static String getFormattedDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
