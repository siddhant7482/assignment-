package com.example.agent;

import java.util.List;
import java.util.Map;

public class CustomerBookingsAggregate {
    public Long customerId;
    public List<TaxiBookingDto> taxi;
    public List<HotelBookingDto> hotel;
    public List<FlightBookingDto> flight;
    public Map<String, String> errors; // service -> message
}