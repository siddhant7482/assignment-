package com.example.taxi;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TaxiCreate {
    @NotBlank
    @Size(min = 7, max = 7)
    @Pattern(regexp = "^[A-Za-z0-9]{7}$", message = "registration must be 7 alphanumeric characters")
    public String registration;

    @Min(2)
    @Max(20)
    public int seats;
}