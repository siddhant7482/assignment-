package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class FlightBookingCreate {
    @NotNull
    @Positive
    public Long customerId;

    @NotNull
    @Positive
    public Long flightId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public FlightBookingCreate() {}

    public FlightBookingCreate(Long customerId, Long flightId, LocalDate date) {
        this.customerId = customerId;
        this.flightId = flightId;
        this.date = date;
    }
}