package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightBookingDto {
    public Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public Map<String, Object> flight;
    public Map<String, Object> customer;
}