package com.fastappoint.dto;

import java.util.UUID;

public class ResourceDTO {
    private UUID id;
    private UUID businessId;
    private UUID typeId;
    private String name;
    private String typeName;
    private Integer capacity;
    private String mergeGroup;
    private java.util.List<ResourceAttributeValueDTO> attributeValues;

    public ResourceDTO() {
    }

    public ResourceDTO(
            UUID id,
            UUID businessId,
            UUID typeId,
            String name,
            String typeName,
            Integer capacity,
            String mergeGroup,
            java.util.List<ResourceAttributeValueDTO> attributeValues
    ) {
        this.id = id;
        this.businessId = businessId;
        this.typeId = typeId;
        this.name = name;
        this.typeName = typeName;
        this.capacity = capacity;
        this.mergeGroup = mergeGroup;
        this.attributeValues = attributeValues;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getMergeGroup() {
        return mergeGroup;
    }

    public void setMergeGroup(String mergeGroup) {
        this.mergeGroup = mergeGroup;
    }

    public java.util.List<ResourceAttributeValueDTO> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(java.util.List<ResourceAttributeValueDTO> attributeValues) {
        this.attributeValues = attributeValues;
    }

}
