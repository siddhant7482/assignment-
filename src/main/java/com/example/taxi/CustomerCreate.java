package com.example.taxi;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerCreate {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "name must contain only letters and spaces")
    public String name;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Pattern(regexp = "^0\\d{10}$", message = "phonenumber must start with 0 and be 11 digits")
    public String phonenumber;
}