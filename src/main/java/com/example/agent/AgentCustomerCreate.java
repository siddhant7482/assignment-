package com.example.agent;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AgentCustomerCreate {
    @NotBlank
    public String name;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String phonenumber;
}