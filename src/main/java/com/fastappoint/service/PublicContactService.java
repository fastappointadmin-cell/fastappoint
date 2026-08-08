package com.fastappoint.service;

import com.fastappoint.dto.PublicContactMessageRequest;
import com.fastappoint.exception.ContactDeliveryException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PublicContactService {
    private static final Logger log = LoggerFactory.getLogger(PublicContactService.class);

    private final Resend resend;
    private final boolean enabled;
    private final String apiKey;
    private final String recipientEmail;
    private final String fromEmail;

    public PublicContactService(@Value("${app.contact.enabled:false}") boolean enabled,
                                @Value("${app.contact.resend.api-key:}") String apiKey,
                                @Value("${app.contact.recipient-email}") String recipientEmail,
                                @Value("${app.contact.from-email}") String fromEmail) {
        this.resend = StringUtils.hasText(apiKey) ? new Resend(apiKey) : null;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.recipientEmail = recipientEmail;
        this.fromEmail = fromEmail;
    }

    public void send(PublicContactMessageRequest request) {
        if (!enabled) {
            throw new ContactDeliveryException("Contact form is temporarily unavailable");
        }
        if (!StringUtils.hasText(apiKey) || resend == null) {
            throw new ContactDeliveryException("Mail delivery is not configured");
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(recipientEmail)
                .replyTo(request.getEmail().trim())
                .subject("FastAppoint contact form: " + request.getName().trim())
                .text(buildBody(request))
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException ex) {
            log.error("Failed to deliver contact message via Resend", ex);
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
