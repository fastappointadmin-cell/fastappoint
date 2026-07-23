package com.fastappoint.dto;

public class CreateBusinessRequest {
    private String name;

    public CreateBusinessRequest() {}

    public CreateBusinessRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}