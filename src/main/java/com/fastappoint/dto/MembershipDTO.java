package com.fastappoint.dto;

import java.util.UUID;

public class MembershipDTO {
    private UUID businessId;
    private String businessName;
    private String role;

    public MembershipDTO() {}

    public MembershipDTO(UUID businessId, String businessName, String role) {
        this.businessId = businessId;
        this.businessName = businessName;
        this.role = role;
    }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
