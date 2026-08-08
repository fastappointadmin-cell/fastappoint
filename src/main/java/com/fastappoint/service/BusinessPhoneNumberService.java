package com.fastappoint.service;

import com.fastappoint.exception.InvalidAppointmentException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.fastappoint.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Phone number parsing, validation and normalization backed by Google's libphonenumber.
 * All numbers are stored in E.164 format (e.g. +40741234567).
 * Romanian numbers without a country prefix are assumed to be Romanian (default region RO).
 */
@Service
public class BusinessPhoneNumberService {

    private static final String DEFAULT_PREFIX = "+4074";
    private static final String DEFAULT_REGION = "RO";

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final BusinessRepository businesses;

    public BusinessPhoneNumberService(BusinessRepository businesses) {
        this.businesses = businesses;
    }

    /**
     * Parses and normalizes any phone number string to E.164.
     * Accepts local Romanian numbers (e.g. "0741234567"), international format ("+40741234567"),
     * and numbers with spaces or dashes.
     * Returns empty string for null/blank input (preserves existing lenient behaviour for optional fields).
     */
    public String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            return "";
        }
        try {
            PhoneNumber parsed = phoneUtil.parse(rawPhoneNumber, DEFAULT_REGION);
            return phoneUtil.format(parsed, PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            // Fall back to stripping non-digit chars and keeping whatever is there,
            // matching the old behaviour so existing data isn't broken.
            String digits = rawPhoneNumber.replaceAll("[^0-9+]", "");
            return digits.isEmpty() ? "" : digits;
        }
    }

    /**
     * Parses, validates, and normalizes to E.164.
     * Throws InvalidAppointmentException with a user-friendly message if the number is invalid.
     */
    public String validateAndNormalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new InvalidAppointmentException("Phone number is required");
        }
        try {
            PhoneNumber parsed = phoneUtil.parse(rawPhoneNumber, DEFAULT_REGION);
            if (!phoneUtil.isValidNumber(parsed)) {
                throw new InvalidAppointmentException("Invalid phone number: " + rawPhoneNumber);
            }
            return phoneUtil.format(parsed, PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new InvalidAppointmentException("Invalid phone number: " + rawPhoneNumber);
        }
    }

    public String normalizeOrGenerate(String requestedPhoneNumber) {
        String normalized = normalize(requestedPhoneNumber);
        return normalized.isEmpty() ? generateUniqueDefaultNumber() : normalized;
    }

    public String generateUniqueDefaultNumber() {
        String candidate;
        do {
            candidate = DEFAULT_PREFIX + sixDigits();
        } while (businesses.existsByChatPhoneNumber(candidate));
        return candidate;
    }

    private String sixDigits() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
