package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.exception.WhatsAppConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

/**
 * The single Meta WhatsApp Cloud API integration for the whole platform: one system-user access
 * token, one WhatsApp Business Account (WABA), many phone numbers registered under it -- one per
 * connected business (see {@link com.fastappoint.domain.WhatsAppConnection}). Businesses never see
 * or touch any of this; FastAppoint holds the one account the original brainstorm was about.
 *
 * <p>Gated by {@code app.whatsapp.enabled}: disabled (the default) simulates every call so the
 * connection flow and the webhook routing are fully exercisable without live Meta credentials. The
 * endpoint shapes below follow the Cloud API's documented flow as of this writing but are unverified
 * against a live WABA -- confirm against current Meta docs when wiring real credentials.
 */
@Service
public class WhatsAppCloudApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppCloudApiClient.class);
    private static final String SIMULATED_ID_PREFIX = "sim-meta-";

    private final RestClient restClient;
    private final boolean enabled;
    private final String wabaId;
    private final String accessToken;
    private final String webhookVerifyToken;

    public WhatsAppCloudApiClient(RestClient.Builder restClientBuilder,
                                  @Value("${app.whatsapp.enabled:false}") boolean enabled,
                                  @Value("${app.whatsapp.meta.base-url:https://graph.facebook.com/v21.0}") String baseUrl,
                                  @Value("${app.whatsapp.meta.waba-id:}") String wabaId,
                                  @Value("${app.whatsapp.meta.access-token:}") String accessToken,
                                  @Value("${app.whatsapp.webhook-verify-token:}") String webhookVerifyToken) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.wabaId = wabaId;
        this.accessToken = accessToken;
        this.webhookVerifyToken = webhookVerifyToken;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getWebhookVerifyToken() {
        return webhookVerifyToken;
    }

    /** Registers a number against the shared WABA. Returns Meta's {@code phone_number_id} -- the
     * identifier every subsequent call (verify, profile, send, deregister) and the inbound webhook
     * route by. */
    @SuppressWarnings("unchecked")
    public String addNumber(String e164Number) {
        if (!enabled) {
            String simulated = SIMULATED_ID_PREFIX + UUID.randomUUID();
            LOG.info("WhatsApp Cloud API disabled -- simulated phone_number_id {} for {}", simulated, e164Number);
            return simulated;
        }
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/{wabaId}/phone_numbers", wabaId)
                    .header("Authorization", bearer())
                    .body(Map.of("cc", "", "phone_number", e164Number))
                    .retrieve()
                    .body(Map.class);
            Object id = response == null ? null : response.get("id");
            if (id == null) {
                throw new WhatsAppConnectionException("Meta did not return a phone_number_id for " + e164Number);
            }
            return String.valueOf(id);
        } catch (RestClientException ex) {
            LOG.warn("Failed to register {} with Meta: {}", e164Number, ex.getMessage());
            throw new WhatsAppConnectionException("Could not register the number with WhatsApp. Please try again.");
        }
    }

    /** Triggers Meta to send a verification code (SMS by default) to the number being registered. */
    public void requestVerificationCode(String phoneNumberId) {
        if (!enabled || phoneNumberId.startsWith(SIMULATED_ID_PREFIX)) {
            LOG.info("WhatsApp Cloud API disabled -- simulated verification code request for {}", phoneNumberId);
            return;
        }
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/request_code", phoneNumberId)
                    .header("Authorization", bearer())
                    .body(Map.of("code_method", "SMS", "language", "en"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.warn("Failed to request verification code for {}: {}", phoneNumberId, ex.getMessage());
            throw new WhatsAppConnectionException("Could not send the verification code. Please try again.");
        }
    }

    /** Verifies the code entered by the business owner (OWN_NUMBER path) or read automatically off the
     * provisioned number's inbox (PROVISIONED path). Throws if Meta rejects it. */
    public void verifyCode(String phoneNumberId, String code) {
        if (!enabled || phoneNumberId.startsWith(SIMULATED_ID_PREFIX)) {
            LOG.info("WhatsApp Cloud API disabled -- simulated verification accepted for {}", phoneNumberId);
            return;
        }
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/verify_code", phoneNumberId)
                    .header("Authorization", bearer())
                    .body(Map.of("code", code))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.warn("Verification code rejected for {}: {}", phoneNumberId, ex.getMessage());
            throw new WhatsAppConnectionException("That code wasn't accepted. Double-check it and try again.");
        }
    }

    /** Syncs the WhatsApp contact-card identity (name/description) for this number to the business's
     * own -- this is what makes a shared platform account still show up as "Riverside Studio" to the
     * customer, not "FastAppoint". */
    public void updateProfile(String phoneNumberId, Business business) {
        if (!enabled || phoneNumberId.startsWith(SIMULATED_ID_PREFIX)) {
            LOG.info("WhatsApp Cloud API disabled -- simulated profile sync for {} ({})", phoneNumberId, business.getName());
            return;
        }
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/whatsapp_business_profile", phoneNumberId)
                    .header("Authorization", bearer())
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "about", business.getDescription() == null || business.getDescription().isBlank()
                                    ? business.getName() : business.getDescription()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            // Non-fatal: the connection is still usable without a synced profile, just less recognizable.
            LOG.warn("Failed to sync WhatsApp profile for {}: {}", phoneNumberId, ex.getMessage());
        }
    }

    public void deregisterNumber(String phoneNumberId) {
        if (phoneNumberId == null || phoneNumberId.isBlank() || phoneNumberId.startsWith(SIMULATED_ID_PREFIX) || !enabled) {
            return;
        }
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/deregister", phoneNumberId)
                    .header("Authorization", bearer())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.warn("Failed to deregister {}: {}", phoneNumberId, ex.getMessage());
        }
    }

    /** Sends a text reply from the business's number back to the customer. Used both for the AI chat
     * reply and (once wired) confirmation/reminder messages. */
    public void sendMessage(String phoneNumberId, String toE164Number, String body) {
        if (!enabled || phoneNumberId == null || phoneNumberId.startsWith(SIMULATED_ID_PREFIX)) {
            LOG.info("WhatsApp Cloud API disabled -- simulated send from {} to {}: {}", phoneNumberId, toE164Number, body);
            return;
        }
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .header("Authorization", bearer())
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "to", toE164Number,
                            "type", "text",
                            "text", Map.of("body", body)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.warn("Failed to send WhatsApp message via {} to {}: {}", phoneNumberId, toE164Number, ex.getMessage());
        }
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
