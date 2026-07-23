package com.fastappoint.dto;

import java.util.List;
import java.util.UUID;

public class BusinessDTO {
    private UUID id;
    private String name;
    private List<ServiceDTO> services;
    private List<ResourceDTO> resources;

    public BusinessDTO() {}

    public BusinessDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public BusinessDTO(UUID id, String name, List<ServiceDTO> services, List<ResourceDTO> resources) {
        this.id = id;
        this.name = name;
        this.services = services;
        this.resources = resources;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ServiceDTO> getServices() { return services; }
    public void setServices(List<ServiceDTO> services) { this.services = services; }

    public List<ResourceDTO> getResources() { return resources; }
    public void setResources(List<ResourceDTO> resources) { this.resources = resources; }
}