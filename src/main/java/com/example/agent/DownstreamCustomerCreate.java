package com.example.agent;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class DownstreamCustomerCreate {
    @NotBlank
    public String name;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String phonenumber;

    public DownstreamCustomerCreate() {}

    public DownstreamCustomerCreate(String name, String email, String phonenumber) {
        this.name = name;
        this.email = email;
        this.phonenumber = phonenumber;
    }
}