package com.fastappoint.dto;

import com.fastappoint.domain.ResourceAttributeType;

import java.util.List;

public class CreateResourceAttributeDefinitionRequest {
    private String name;
    private ResourceAttributeType type;
    private Boolean required;
    private List<String> options;

    public CreateResourceAttributeDefinitionRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceAttributeType getType() { return type; }
    public void setType(ResourceAttributeType type) { this.type = type; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
