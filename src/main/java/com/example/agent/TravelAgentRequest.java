package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TravelAgentRequest {
    @NotNull
    @Positive
    public Long customerId;

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