package com.fastappoint.service;

import com.fastappoint.exception.WhatsAppConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Buys/releases the virtual numbers used for {@link com.fastappoint.domain.WhatsAppConnectionSource#PROVISIONED}
 * connections, via Twilio's REST API. Gated by {@code app.whatsapp.enabled}: when disabled (the
 * default, and every environment without real Twilio + Meta credentials), every call is simulated --
 * this keeps the whole connection flow demoable/testable end-to-end before those credentials exist.
 *
 * <p>The Twilio endpoints/params below follow their documented REST API shape as of this writing but
 * are unverified against a live account -- confirm against current Twilio docs when wiring real
 * credentials for the first time.
 */
@Service
public class WhatsAppNumberProvider {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppNumberProvider.class);
    private static final String SIMULATED_SID_PREFIX = "sim-";

    public record ProvisionedNumber(String e164Number, String providerNumberSid) {
    }

    private final RestClient restClient;
    private final BusinessPhoneNumberService phoneNumbers;
    private final boolean enabled;
    private final String accountSid;
    private final String authToken;
    private final String numberCountry;

    public WhatsAppNumberProvider(RestClient.Builder restClientBuilder,
                                  BusinessPhoneNumberService phoneNumbers,
                                  @Value("${app.whatsapp.enabled:false}") boolean enabled,
                                  @Value("${app.whatsapp.twilio.base-url:https://api.twilio.com/2010-04-01}") String baseUrl,
                                  @Value("${app.whatsapp.twilio.account-sid:}") String accountSid,
                                  @Value("${app.whatsapp.twilio.auth-token:}") String authToken,
                                  @Value("${app.whatsapp.number-country:RO}") String numberCountry) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.phoneNumbers = phoneNumbers;
        this.enabled = enabled;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.numberCountry = numberCountry;
    }

    public ProvisionedNumber purchaseNumber() {
        if (!enabled || accountSid.isBlank() || authToken.isBlank()) {
            String simulatedNumber = phoneNumbers.generateUniqueDefaultNumber();
            LOG.info("WhatsApp provisioning disabled -- simulated number {} for a PROVISIONED connection", simulatedNumber);
            return new ProvisionedNumber(simulatedNumber, SIMULATED_SID_PREFIX + java.util.UUID.randomUUID());
        }

        try {
            String available = searchAvailableNumber();
            return purchase(available);
        } catch (RestClientException ex) {
            LOG.warn("Twilio number purchase failed: {}", ex.getMessage());
            throw new WhatsAppConnectionException("Could not obtain a new WhatsApp number right now. Please try again shortly.");
        }
    }

    /** Best-effort read of the verification code Meta just texted to a Twilio-purchased number --
     * what makes the {@code PROVISIONED} path fully automated (no OTP screen shown to the business).
     * Polls Twilio's own inbound-message log rather than requiring a separate SMS webhook. Returns
     * null if nothing arrived within the timeout, in which case the caller fails the connection
     * attempt with a clear reason instead of leaving it stuck. */
    public String awaitVerificationCode(String providerNumberSid, java.time.Duration timeout) {
        if (!enabled || providerNumberSid == null || providerNumberSid.startsWith(SIMULATED_SID_PREFIX)) {
            return null;
        }
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            String code = extractCodeFromLatestSms(providerNumberSid);
            if (code != null) {
                return code;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCodeFromLatestSms(String providerNumberSid) {
        try {
            String phoneNumber = fetchNumberForSid(providerNumberSid);
            Map<String, Object> response = restClient.get()
                    .uri("/Accounts/{accountSid}/Messages.json?To={to}&PageSize=5", accountSid, phoneNumber)
                    .header("Authorization", basicAuth())
                    .retrieve()
                    .body(Map.class);
            List<Map<String, Object>> messages = response == null ? List.of() : (List<Map<String, Object>>) response.get("messages");
            if (messages == null) {
                return null;
            }
            for (Map<String, Object> message : messages) {
                String body = String.valueOf(message.getOrDefault("body", ""));
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d{5,8})\\b").matcher(body);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            return null;
        } catch (RestClientException ex) {
            LOG.warn("Failed to poll Twilio SMS log for {}: {}", providerNumberSid, ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String fetchNumberForSid(String providerNumberSid) {
        Map<String, Object> response = restClient.get()
                .uri("/Accounts/{accountSid}/IncomingPhoneNumbers/{sid}.json", accountSid, providerNumberSid)
                .header("Authorization", basicAuth())
                .retrieve()
                .body(Map.class);
        return response == null ? null : String.valueOf(response.get("phone_number"));
    }

    public void releaseNumber(String providerNumberSid) {
        if (providerNumberSid == null || providerNumberSid.isBlank() || providerNumberSid.startsWith(SIMULATED_SID_PREFIX)) {
            LOG.info("Skipping provider release for simulated/blank SID {}", providerNumberSid);
            return;
        }
        if (!enabled) {
            return;
        }
        try {
            restClient.delete()
                    .uri("/Accounts/{accountSid}/IncomingPhoneNumbers/{sid}.json", accountSid, providerNumberSid)
                    .header("Authorization", basicAuth())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.warn("Failed to release Twilio number {}: {}", providerNumberSid, ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String searchAvailableNumber() {
        Map<String, Object> response = restClient.get()
                .uri("/Accounts/{accountSid}/AvailablePhoneNumbers/{country}/Local.json?SmsEnabled=true&VoiceEnabled=true",
                        accountSid, numberCountry)
                .header("Authorization", basicAuth())
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> numbers = response == null ? List.of() : (List<Map<String, Object>>) response.get("available_phone_numbers");
        if (numbers == null || numbers.isEmpty()) {
            throw new WhatsAppConnectionException("No numbers currently available from the provider for " + numberCountry);
        }
        return String.valueOf(numbers.get(0).get("phone_number"));
    }

    @SuppressWarnings("unchecked")
    private ProvisionedNumber purchase(String phoneNumber) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("PhoneNumber", phoneNumber);

        Map<String, Object> response = restClient.post()
                .uri("/Accounts/{accountSid}/IncomingPhoneNumbers.json", accountSid)
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("sid") == null) {
            throw new WhatsAppConnectionException("Provider did not confirm the number purchase");
        }
        return new ProvisionedNumber(phoneNumber, String.valueOf(response.get("sid")));
    }

    private String basicAuth() {
        String credentials = accountSid + ":" + authToken;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
