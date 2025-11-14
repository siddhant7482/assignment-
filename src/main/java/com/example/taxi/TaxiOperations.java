package com.example.taxi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDate;
import java.util.List;

/**
 * Minimal local service to wrap Taxi persistence operations so the Travel Agent
 * can call into the same Quarkus application without HTTP.
 */
@ApplicationScoped
public class TaxiOperations {

    @Inject
    BookingRepository bookingRepo;

    @Inject
    CustomerRepository customerRepo;

    @Inject
    TaxiRepository taxiRepo;

    /**
     * Returns an existing customer by email or creates a new one.
     */
    @Transactional
    public Customer ensureCustomerByEmail(String name, String email, String phonenumber) {
        if (email == null || email.isBlank()) {
            throw new WebApplicationException("Email is required", 400);
        }
        Customer existing = customerRepo.findByEmail(email);
        if (existing != null) {
            return existing;
        }
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        c.setPhonenumber(phonenumber);
        return customerRepo.create(c);
    }

    /**
     * Create a taxi booking enforcing the same rules as the REST resource:
     * - customerId and taxiId must exist
     * - (tax & date) must be unique
     */
    @Transactional
    public Booking createTaxiBooking(Long customerId, Long taxiId, LocalDate date) {
        if (customerId == null || customerId <= 0) {
            throw new WebApplicationException("Invalid customerId", 400);
        }
        if (taxiId == null || taxiId <= 0) {
            throw new WebApplicationException("Invalid taxiId", 400);
        }
        if (date == null) {
            throw new WebApplicationException("date is required", 400);
        }

        Customer customer = customerRepo.findById(customerId);
        if (customer == null) {
            throw new WebApplicationException("Invalid customerId", 400);
        }
        Taxi taxi = taxiRepo.findById(taxiId);
        if (taxi == null) {
            throw new WebApplicationException("Invalid taxiId", 400);
        }

        if (bookingRepo.findByTaxiAndDate(taxi, date) != null) {
            throw new WebApplicationException("Booking already exists for taxi and date", 409);
        }

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setTaxi(taxi);
        b.setDate(date);
        return bookingRepo.create(b);
    }

    /**
     * Lists bookings for a given customer.
     */
    public List<Booking> listTaxiBookingsForCustomer(Long customerId) {
        return bookingRepo.listByCustomerId(customerId);
    }

    /**
     * Cancels a booking by id.
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean cancelTaxiBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new WebApplicationException("Invalid bookingId", 400);
        }
        return bookingRepo.deleteById(bookingId);
    }
}