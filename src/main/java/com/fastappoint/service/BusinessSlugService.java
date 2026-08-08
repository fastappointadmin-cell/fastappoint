package com.fastappoint.service;

import com.fastappoint.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns a business name into the URL-safe slug used as its booking subdomain
 * ({@code slug}.fastappoint.app). Two concerns live here, not on the entity: picking a slug needs a
 * database round-trip (uniqueness) plus a reserved-word list that only this layer should know about.
 */
@Service
public class BusinessSlugService {

    /** Subdomains the app itself needs or might need later -- a business can't claim one of these,
     * however close its name comes, or it would shadow a real system route. */
    private static final Set<String> RESERVED = Set.of(
            "www", "app", "api", "admin", "mail", "ftp", "static", "assets", "cdn",
            "book", "booking", "dashboard", "auth", "login", "register", "oauth2", "docs", "status"
    );

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");

    private final BusinessRepository businesses;

    public BusinessSlugService(BusinessRepository businesses) {
        this.businesses = businesses;
    }

    /** A slug guaranteed free and not reserved -- appends "-2", "-3", etc. on collision. */
    public String generateUniqueSlug(String businessName) {
        String base = slugify(businessName);
        String candidate = base;
        int suffix = 2;
        while (RESERVED.contains(candidate) || businesses.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String slugify(String input) {
        String withoutDiacritics = DIACRITICS.matcher(Normalizer.normalize(input, Normalizer.Form.NFD)).replaceAll("");
        String slug = EDGE_DASHES.matcher(
                NON_ALPHANUMERIC.matcher(withoutDiacritics.toLowerCase()).replaceAll("-")
        ).replaceAll("");
        return slug.isEmpty() ? "business" : slug;
    }
}
