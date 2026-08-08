package com.fastappoint.service;

import com.fastappoint.dto.PublicContactMessageRequest;
import com.fastappoint.exception.ContactDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class PublicContactService {
    private static final Logger log = LoggerFactory.getLogger(PublicContactService.class);

    private final RestClient restClient;
    private final boolean enabled;
    private final String apiKey;
    private final String recipientEmail;
    private final String fromEmail;

    public PublicContactService(RestClient.Builder restClientBuilder,
                                @Value("${app.contact.enabled:false}") boolean enabled,
                                @Value("${app.contact.resend.api-url:https://api.resend.com/emails}") String apiUrl,
                                @Value("${app.contact.resend.api-key:}") String apiKey,
                                @Value("${app.contact.recipient-email}") String recipientEmail,
                                @Value("${app.contact.from-email}") String fromEmail) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.recipientEmail = recipientEmail;
        this.fromEmail = fromEmail;
    }

    public void send(PublicContactMessageRequest request) {
        if (!enabled) {
            throw new ContactDeliveryException("Contact form is temporarily unavailable");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new ContactDeliveryException("Mail delivery is not configured");
        }

        Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(recipientEmail),
                "reply_to", request.getEmail().trim(),
                "subject", "FastAppoint contact form: " + request.getName().trim(),
                "text", buildBody(request));

        try {
            restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
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
