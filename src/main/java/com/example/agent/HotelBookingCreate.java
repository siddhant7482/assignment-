package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class HotelBookingCreate {
    @NotNull
    @Positive
    public Long customerId;

    @NotNull
    @Positive
    public Long hotelId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public HotelBookingCreate() {}

    public HotelBookingCreate(Long customerId, Long hotelId, LocalDate date) {
        this.customerId = customerId;
        this.hotelId = hotelId;
        this.date = date;
    }
}