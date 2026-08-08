package com.fastappoint.service;

import com.fastappoint.dto.PublicContactMessageRequest;
import com.fastappoint.exception.ContactDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PublicContactService {
    @Nullable
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String recipientEmail;
    private final String fromEmail;
    private final String mailHost;

    public PublicContactService(@Nullable JavaMailSender mailSender,
                                @Value("${app.contact.enabled:false}") boolean enabled,
                                @Value("${app.contact.recipient-email}") String recipientEmail,
                                @Value("${app.contact.from-email}") String fromEmail,
                                @Value("${spring.mail.username:contactfastappoint@gmail.com}") String mailUsername,
                                @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.recipientEmail = recipientEmail;
        this.fromEmail = StringUtils.hasText(fromEmail) ? fromEmail : mailUsername;
        this.mailHost = mailHost;
    }

    public void send(PublicContactMessageRequest request) {
        if (!enabled) {
            throw new ContactDeliveryException("Contact form is temporarily unavailable");
        }
        if (mailSender == null) {
            throw new ContactDeliveryException("Mail delivery is not configured");
        }
        if (!StringUtils.hasText(mailHost)) {
            throw new ContactDeliveryException("Mail delivery is not configured");
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
