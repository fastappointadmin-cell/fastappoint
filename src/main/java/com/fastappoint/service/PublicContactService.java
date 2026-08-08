package com.fastappoint.service;

import com.fastappoint.dto.PublicContactMessageRequest;
import com.fastappoint.exception.ContactDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PublicContactService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String recipientEmail;
    private final String fromEmail;

    public PublicContactService(JavaMailSender mailSender,
                                @Value("${app.contact.enabled:false}") boolean enabled,
                                @Value("${app.contact.recipient-email}") String recipientEmail,
                                @Value("${app.contact.from-email}") String fromEmail) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.recipientEmail = recipientEmail;
        this.fromEmail = fromEmail;
    }

    public void send(PublicContactMessageRequest request) {
        if (!enabled) {
            throw new ContactDeliveryException("Contact form is temporarily unavailable");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(fromEmail);
        message.setReplyTo(request.getEmail().trim());
        message.setSubject("FastAppoint contact form: " + request.getName().trim());
        message.setText(buildBody(request));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ContactDeliveryException("Could not deliver contact message", ex);
        }
    }

    private String buildBody(PublicContactMessageRequest request) {
        return """
                New contact message from fastappoint.app

                Name: %s
                Email: %s

                Message:
                %s
                """.formatted(
                request.getName().trim(),
                request.getEmail().trim(),
                request.getMessage().trim());
    }
}
