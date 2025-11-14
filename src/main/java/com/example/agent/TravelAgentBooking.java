package com.example.agent;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "travel_agent_bookings")
public class TravelAgentBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDate date;

    // IDs returned from the downstream services
    private Long taxiBookingId;
    private Long hotelBookingId;
    private Long flightBookingId;

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTaxiBookingId() {
        return taxiBookingId;
    }

    public void setTaxiBookingId(Long taxiBookingId) {
        this.taxiBookingId = taxiBookingId;
    }

    public Long getHotelBookingId() {
        return hotelBookingId;
    }

    public void setHotelBookingId(Long hotelBookingId) {
        this.hotelBookingId = hotelBookingId;
    }

    public Long getFlightBookingId() {
        return flightBookingId;
    }

    public void setFlightBookingId(Long flightBookingId) {
        this.flightBookingId = flightBookingId;
    }
}