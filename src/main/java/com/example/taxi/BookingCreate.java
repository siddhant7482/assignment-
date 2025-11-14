package com.example.taxi;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BookingCreate {
    @NotNull
    public Long customerId;

    @NotNull
    public Long taxiId;

    @NotNull
    @Future
    public LocalDate date;
}