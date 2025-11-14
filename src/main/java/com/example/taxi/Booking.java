package com.example.taxi;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_taxi_date", columnNames = {"taxi_id", "booking_date"})
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "taxi_id", nullable = false)
    private Taxi taxi;

    @NotNull
    @Future
    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Taxi getTaxi() { return taxi; }
    public void setTaxi(Taxi taxi) { this.taxi = taxi; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}