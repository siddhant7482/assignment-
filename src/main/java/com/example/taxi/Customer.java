package com.example.taxi;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = {"email"})
})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "name must contain only letters and spaces")
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Pattern(regexp = "^0\\d{10}$", message = "phonenumber must start with 0 and be 11 digits")
    private String phonenumber;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}