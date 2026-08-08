package com.fastappoint.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The tenant boundary and aggregate root for a business's configuration.
 * Resource types are resolved by name through {@link #resourceTypeNamed(String)},
 * which finds an existing one or creates it.
 */
@Entity
@Table(name = "business")
public class Business {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    /** URL-safe, globally-unique identifier used as the business's booking subdomain
     * ({@code slug}.fastappoint.app). Generated once at creation time (see
     * {@link com.fastappoint.service.BusinessSlugService}) and never changed afterward, even if the
     * business is renamed -- a stable slug means booking links/QR codes never silently break. */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "chat_phone_number", nullable = false, unique = true, length = 32)
    private String chatPhoneNumber;

    @Column(name = "business_description", nullable = false, length = 4000)
    private String description = "";

    @Column(nullable = false, length = 2000)
    private String confirmationMessage = "";

    @Column(nullable = false, length = 2000)
    private String confirmationLocationInfo = "";

    @Column(nullable = false, length = 2000)
    private String confirmationGoogleMapsLink = "";

    @Column(nullable = false)
    private boolean confirmationEnabled = true;

    @Column(nullable = false)
    private boolean confirmationIncludeLocationInfo = false;

    @Column(nullable = false)
    private boolean confirmationIncludeTime = true;

    @Column(nullable = false)
    private boolean confirmationIncludeBookingSlot = true;

    @Column(nullable = false, length = 2000)
    private String reminderMessage = "";

    @Column(nullable = false, length = 2000)
    private String reminderLocationInfo = "";

    @Column(nullable = false, length = 2000)
    private String reminderGoogleMapsLink = "";

    @Column(nullable = false)
    private boolean reminderEnabled = true;

    @Column(nullable = false)
    private boolean reminderIncludeLocationInfo = false;

    @Column(nullable = false)
    private boolean reminderIncludeTime = true;

    @Column(nullable = false)
    private boolean reminderIncludeBookingSlot = true;

    @Column(nullable = false)
    private int reminderLeadTimeMinutes = 1440;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceType> resourceTypes = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessService> businessServices = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessMembership> memberships = new ArrayList<>();

    protected Business() { // required by Hibernate
    }

    public Business(String name, String slug, String chatPhoneNumber, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.slug = slug;
        this.chatPhoneNumber = chatPhoneNumber;
        this.description = description;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateChatIdentity(String chatPhoneNumber, String description) {
        this.chatPhoneNumber = chatPhoneNumber;
        this.description = description;
    }

    public void updateConfirmationSettings(String message, String locationInfo, String googleMapsLink,
                                           boolean enabled, boolean includeLocationInfo, boolean includeTime,
                                           boolean includeBookingSlot) {
        this.confirmationMessage = message;
        this.confirmationLocationInfo = locationInfo;
        this.confirmationGoogleMapsLink = googleMapsLink;
        this.confirmationEnabled = enabled;
        this.confirmationIncludeLocationInfo = includeLocationInfo;
        this.confirmationIncludeTime = includeTime;
        this.confirmationIncludeBookingSlot = includeBookingSlot;
    }

    public void updateReminderSettings(String message, String locationInfo, String googleMapsLink,
                                       boolean enabled, boolean includeLocationInfo, boolean includeTime,
                                       boolean includeBookingSlot, int leadTimeMinutes) {
        this.reminderMessage = message;
        this.reminderLocationInfo = locationInfo;
        this.reminderGoogleMapsLink = googleMapsLink;
        this.reminderEnabled = enabled;
        this.reminderIncludeLocationInfo = includeLocationInfo;
        this.reminderIncludeTime = includeTime;
        this.reminderIncludeBookingSlot = includeBookingSlot;
        this.reminderLeadTimeMinutes = leadTimeMinutes;
    }

    /**
     * Find-or-create the type for this name within this business. Matching is
     * case-insensitive on the trimmed name; the first spelling seen is kept.
     */
    public ResourceType resourceTypeNamed(String typeName) {
        String normalized = typeName.trim();
        for (ResourceType existing : resourceTypes) {
            if (existing.getName().equalsIgnoreCase(normalized)) {
                return existing;
            }
        }
        ResourceType created = new ResourceType(this, normalized);
        resourceTypes.add(created);
        return created;
    }

    /** Add a resource, resolving its type by name (creating the type if new). */
    public Resource addResource(String resourceName, String typeName) {
        Resource resource = new Resource(this, resourceName, resourceTypeNamed(typeName));
        resources.add(resource);
        return resource;
    }

    /** Add a resource using an explicitly pre-created type. */
    public Resource addResource(String resourceName, ResourceType type) {
        Resource resource = new Resource(this, resourceName, type);
        resources.add(resource);
        return resource;
    }

    public BusinessService addService(String serviceName, java.time.Duration duration) {
        BusinessService businessService = new BusinessService(this, serviceName, duration);
        businessServices.add(businessService);
        return businessService;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getChatPhoneNumber() { return chatPhoneNumber; }
    public String getDescription() { return description; }
    public String getConfirmationMessage() { return confirmationMessage; }
    public String getConfirmationLocationInfo() { return confirmationLocationInfo; }
    public String getConfirmationGoogleMapsLink() { return confirmationGoogleMapsLink; }
    public boolean isConfirmationEnabled() { return confirmationEnabled; }
    public boolean isConfirmationIncludeLocationInfo() { return confirmationIncludeLocationInfo; }
    public boolean isConfirmationIncludeTime() { return confirmationIncludeTime; }
    public boolean isConfirmationIncludeBookingSlot() { return confirmationIncludeBookingSlot; }
    public String getReminderMessage() { return reminderMessage; }
    public String getReminderLocationInfo() { return reminderLocationInfo; }
    public String getReminderGoogleMapsLink() { return reminderGoogleMapsLink; }
    public boolean isReminderEnabled() { return reminderEnabled; }
    public boolean isReminderIncludeLocationInfo() { return reminderIncludeLocationInfo; }
    public boolean isReminderIncludeTime() { return reminderIncludeTime; }
    public boolean isReminderIncludeBookingSlot() { return reminderIncludeBookingSlot; }
    public int getReminderLeadTimeMinutes() { return reminderLeadTimeMinutes; }
    public List<ResourceType> getResourceTypes() { return resourceTypes; }
    public List<Resource> getResources() { return resources; }
    public List<BusinessService> getServices() { return businessServices; }
    public List<BusinessMembership> getMemberships() { return memberships; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Business other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
