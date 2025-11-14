package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TaxiBookingCreate {
    @NotNull
    @Positive
    public Long customerId;

    @NotNull
    @Positive
    public Long taxiId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public TaxiBookingCreate() {}

    public TaxiBookingCreate(Long customerId, Long taxiId, LocalDate date) {
        this.customerId = customerId;
        this.taxiId = taxiId;
        this.date = date;
    }
}