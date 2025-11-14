package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelBookingDto {
    public Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public Map<String, Object> hotel;
    public Map<String, Object> customer;
}