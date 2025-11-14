package com.example.taxi;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "taxis", uniqueConstraints = {
        @UniqueConstraint(name = "uk_taxi_registration", columnNames = {"registration"})
})
public class Taxi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 7, max = 7)
    @Pattern(regexp = "^[A-Za-z0-9]{7}$", message = "registration must be 7 alphanumeric characters")
    @Column(nullable = false, length = 7)
    private String registration;

    @Min(2)
    @Max(20)
    @Column(nullable = false)
    private int seats;

    @OneToMany(mappedBy = "taxi", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings;

    public Long getId() { return id; }
    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}