package com.example.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxiBookingDto {
    public Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    // Downstream includes nested taxi/customer objects; keep them as maps for flexibility
    public Map<String, Object> taxi;
    public Map<String, Object> customer;
}