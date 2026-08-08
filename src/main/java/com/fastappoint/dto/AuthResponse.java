package com.fastappoint.dto;

import java.util.List;

public class AuthResponse {
    private String accessToken;
    private UserDTO user;
    private List<MembershipDTO> memberships;

    public AuthResponse() {}

    public AuthResponse(String accessToken, UserDTO user, List<MembershipDTO> memberships) {
        this.accessToken = accessToken;
        this.user = user;
        this.memberships = memberships;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public List<MembershipDTO> getMemberships() { return memberships; }
    public void setMemberships(List<MembershipDTO> memberships) { this.memberships = memberships; }
}
