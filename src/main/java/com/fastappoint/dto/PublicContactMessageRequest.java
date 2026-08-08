package com.fastappoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PublicContactMessageRequest {
    @NotBlank(message = "Contact name is required")
    @Size(max = 120, message = "Contact name is too long")
    private String name;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    @Size(max = 320, message = "Contact email is too long")
    private String email;

    @NotBlank(message = "Contact message is required")
    @Size(max = 5000, message = "Contact message is too long")
    private String message;

    public PublicContactMessageRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
