package com.fastappoint.dto;

import java.util.List;
import java.util.UUID;

public class UpdateResourceRequest {
	private String name;
	private UUID typeId;
	private Integer capacity;
	private String mergeGroup;
	private List<ResourceAttributeValueInput> attributeValues;

	public UpdateResourceRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getTypeId() {
		return typeId;
	}

	public void setTypeId(UUID typeId) {
		this.typeId = typeId;
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

	public List<ResourceAttributeValueInput> getAttributeValues() {
		return attributeValues;
	}

	public void setAttributeValues(List<ResourceAttributeValueInput> attributeValues) {
		this.attributeValues = attributeValues;
	}
}
