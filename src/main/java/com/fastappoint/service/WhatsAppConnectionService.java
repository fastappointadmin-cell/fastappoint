package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.WhatsAppConnection;
import com.fastappoint.domain.WhatsAppConnectionSource;
import com.fastappoint.domain.WhatsAppConnectionStatus;
import com.fastappoint.dto.StartWhatsAppConnectionRequest;
import com.fastappoint.dto.SubmitWhatsAppOtpRequest;
import com.fastappoint.dto.WhatsAppConnectionDTO;
import com.fastappoint.exception.WhatsAppConnectionException;
import com.fastappoint.repository.WhatsAppConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates connecting a business to the shared WhatsApp Business Account (see
 * {@link WhatsAppCloudApiClient} for the "one account" side of this). Two paths:
 * <ul>
 *   <li>{@link WhatsAppConnectionSource#PROVISIONED} -- a new number is bought and verified fully
 *   automatically; the business never sees an OTP step.</li>
 *   <li>{@link WhatsAppConnectionSource#OWN_NUMBER} -- the business's existing number is registered,
 *   Meta texts a code to it, and {@link #submitOtp} completes the connection once they enter it.</li>
 * </ul>
 */
@Service
@Transactional
public class WhatsAppConnectionService {

    private static final Duration PROVISIONED_OTP_TIMEOUT = Duration.ofSeconds(45);

    private final WhatsAppConnectionRepository connections;
    private final BusinessService businessService;
    private final BusinessPhoneNumberService phoneNumbers;
    private final WhatsAppNumberProvider numberProvider;
    private final WhatsAppCloudApiClient cloudApiClient;

    public WhatsAppConnectionService(WhatsAppConnectionRepository connections,
                                     BusinessService businessService,
                                     BusinessPhoneNumberService phoneNumbers,
                                     WhatsAppNumberProvider numberProvider,
                                     WhatsAppCloudApiClient cloudApiClient) {
        this.connections = connections;
        this.businessService = businessService;
        this.phoneNumbers = phoneNumbers;
        this.numberProvider = numberProvider;
        this.cloudApiClient = cloudApiClient;
    }

    @Transactional(readOnly = true)
    public WhatsAppConnectionDTO getConnection(UUID businessId) {
        return connections.findByBusiness_Id(businessId)
                .map(this::toDto)
                .orElseGet(() -> notConnected(businessId));
    }

    public WhatsAppConnectionDTO startConnection(UUID businessId, StartWhatsAppConnectionRequest request) {
        Business business = businessService.getBusinessEntityById(businessId);
        WhatsAppConnectionSource source = parseSource(request.getSource());

        String phoneNumber;
        String providerNumberSid = null;
        if (source == WhatsAppConnectionSource.PROVISIONED) {
            WhatsAppNumberProvider.ProvisionedNumber provisioned = numberProvider.purchaseNumber();
            phoneNumber = provisioned.e164Number();
            providerNumberSid = provisioned.providerNumberSid();
        } else {
            phoneNumber = phoneNumbers.validateAndNormalize(request.getOwnPhoneNumber());
        }

        WhatsAppConnection connection = connections.findByBusiness_Id(businessId)
                .map(existing -> {
                    existing.restart(source, phoneNumber);
                    return existing;
                })
                .orElseGet(() -> new WhatsAppConnection(business, source, phoneNumber));
        if (providerNumberSid != null) {
            connection.markProviderNumberSid(providerNumberSid);
        }
        connections.save(connection);

        String phoneNumberId = cloudApiClient.addNumber(phoneNumber);
        connection.markMetaPhoneNumberId(phoneNumberId);
        connections.save(connection);
        cloudApiClient.requestVerificationCode(phoneNumberId);

        if (source == WhatsAppConnectionSource.PROVISIONED) {
            String code = cloudApiClient.isEnabled()
                    ? numberProvider.awaitVerificationCode(providerNumberSid, PROVISIONED_OTP_TIMEOUT)
                    : "simulated";
            if (code == null) {
                connection.fail("Couldn't read the verification code automatically. Please try again.");
                connections.save(connection);
                throw new WhatsAppConnectionException(
                        "Couldn't finish setting up the new number automatically. Please try again.");
            }
            cloudApiClient.verifyCode(phoneNumberId, code);
            activate(connection, business, phoneNumberId, phoneNumber);
        }

        return toDto(connection);
    }

    public WhatsAppConnectionDTO submitOtp(UUID businessId, SubmitWhatsAppOtpRequest request) {
        WhatsAppConnection connection = requireConnection(businessId);
        if (connection.getStatus() != WhatsAppConnectionStatus.AWAITING_OTP) {
            throw new WhatsAppConnectionException("This business isn't waiting for a verification code right now.");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new WhatsAppConnectionException("Enter the code you received on WhatsApp.");
        }

        cloudApiClient.verifyCode(connection.getMetaPhoneNumberId(), request.getCode().trim());
        activate(connection, connection.getBusiness(), connection.getMetaPhoneNumberId(), connection.getPhoneNumber());
        return toDto(connection);
    }

    public WhatsAppConnectionDTO disconnect(UUID businessId) {
        WhatsAppConnection connection = requireConnection(businessId);

        cloudApiClient.deregisterNumber(connection.getMetaPhoneNumberId());
        if (connection.getSource() == WhatsAppConnectionSource.PROVISIONED) {
            numberProvider.releaseNumber(connection.getProviderNumberSid());
        }
        connection.disconnect();
        connections.save(connection);

        // Falls back to a fresh simulated identifier so the in-app chat widget keeps working.
        Business business = connection.getBusiness();
        business.updateChatIdentity(phoneNumbers.generateUniqueDefaultNumber(), business.getDescription());

        return toDto(connection);
    }

    private void activate(WhatsAppConnection connection, Business business, String phoneNumberId, String phoneNumber) {
        cloudApiClient.updateProfile(phoneNumberId, business);
        business.updateChatIdentity(phoneNumber, business.getDescription());
        connection.activate();
        connections.save(connection);
    }

    private WhatsAppConnection requireConnection(UUID businessId) {
        return connections.findByBusiness_Id(businessId)
                .orElseThrow(() -> new WhatsAppConnectionException("This business has no WhatsApp connection to manage."));
    }

    private WhatsAppConnectionSource parseSource(String rawSource) {
        if (rawSource == null || rawSource.isBlank()) {
            throw new WhatsAppConnectionException("A connection source is required.");
        }
        try {
            return WhatsAppConnectionSource.valueOf(rawSource.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new WhatsAppConnectionException("Unknown connection source: " + rawSource);
        }
    }

    private WhatsAppConnectionDTO notConnected(UUID businessId) {
        return new WhatsAppConnectionDTO(businessId, false, null, null, null, null, null, null, null);
    }

    private WhatsAppConnectionDTO toDto(WhatsAppConnection connection) {
        boolean connected = connection.getStatus() == WhatsAppConnectionStatus.ACTIVE;
        String waLink = connected ? "https://wa.me/" + connection.getPhoneNumber().replaceAll("[^0-9]", "") : null;
        return new WhatsAppConnectionDTO(
                connection.getBusiness().getId(),
                connected,
                connection.getSource().name(),
                connection.getStatus().name(),
                connection.getPhoneNumber(),
                waLink,
                connection.getFailureReason(),
                connection.getCreatedAt(),
                connection.getUpdatedAt());
    }
}
