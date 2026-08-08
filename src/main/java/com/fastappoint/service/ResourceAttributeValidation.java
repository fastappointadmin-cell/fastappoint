package com.fastappoint.service;

import com.fastappoint.domain.ResourceAttributeDefinition;
import com.fastappoint.domain.ResourceAttributeType;
import com.fastappoint.domain.ServiceRequirementConstraintOperator;
import com.fastappoint.exception.InvalidAppointmentException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ResourceAttributeValidation {

    private ResourceAttributeValidation() {
    }

    static List<String> normalizeOptions(ResourceAttributeType type, List<String> rawOptions) {
        if (type != ResourceAttributeType.SINGLE_SELECT) {
            return List.of();
        }

        if (rawOptions == null || rawOptions.isEmpty()) {
            throw new InvalidAppointmentException("Single-select attributes need at least one option");
        }

        Set<String> uniqueOptions = new LinkedHashSet<>();
        for (String rawOption : rawOptions) {
            String normalizedOption = rawOption == null ? "" : rawOption.trim();
            if (normalizedOption.isEmpty()) {
                continue;
            }
            uniqueOptions.add(normalizedOption);
        }

        if (uniqueOptions.isEmpty()) {
            throw new InvalidAppointmentException("Single-select attributes need at least one option");
        }

        return new ArrayList<>(uniqueOptions);
    }

    static String normalizeStoredValue(ResourceAttributeDefinition definition, String rawValue, boolean required, String context) {
        String normalizedValue = rawValue == null ? "" : rawValue.trim();
        if (normalizedValue.isEmpty()) {
            if (required) {
                throw new InvalidAppointmentException(context + " is required");
            }
            return null;
        }

        return switch (definition.getType()) {
            case TEXT -> normalizedValue;
            case NUMBER -> {
                try {
                    Double.parseDouble(normalizedValue);
                    yield normalizedValue;
                } catch (NumberFormatException ex) {
                    throw new InvalidAppointmentException(context + " must be a valid number");
                }
            }
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(normalizedValue) && !"false".equalsIgnoreCase(normalizedValue)) {
                    throw new InvalidAppointmentException(context + " must be true or false");
                }
                yield normalizedValue.toLowerCase(Locale.ROOT);
            }
            case SINGLE_SELECT -> definition.getOptions().stream()
                    .filter(option -> option.equalsIgnoreCase(normalizedValue))
                    .findFirst()
                    .orElseThrow(() -> new InvalidAppointmentException(context + " must match one of the allowed options"));
        };
    }

    static void validateOperator(ResourceAttributeType type, ServiceRequirementConstraintOperator operator) {
        boolean allowed = switch (type) {
            case TEXT -> operator == ServiceRequirementConstraintOperator.EQUALS
                    || operator == ServiceRequirementConstraintOperator.CONTAINS;
            case NUMBER -> operator == ServiceRequirementConstraintOperator.EQUALS
                    || operator == ServiceRequirementConstraintOperator.GREATER_THAN_OR_EQUAL
                    || operator == ServiceRequirementConstraintOperator.LESS_THAN_OR_EQUAL;
            case BOOLEAN, SINGLE_SELECT -> operator == ServiceRequirementConstraintOperator.EQUALS;
        };

        if (!allowed) {
            throw new InvalidAppointmentException("Operator " + operator + " is not valid for attribute type " + type);
        }
    }
}
