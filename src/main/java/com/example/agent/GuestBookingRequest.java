package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class GuestBookingRequest {
    @NotBlank
    public String name;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String phonenumber;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    @NotNull
    @Positive
    public Long taxiId;

    @NotNull
    @Positive
    public Long hotelId;

    @NotNull
    @Positive
    public Long flightId;
}