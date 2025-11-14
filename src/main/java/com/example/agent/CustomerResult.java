package com.example.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResult {
    public Long id;
    public String name;
    public String email;
    public String phonenumber;
}