package com.example.taxi;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import java.net.URI;

@Path("/api/guest-bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuestBookingResource {

    @Inject
    EntityManager em;

    // Use declarative transactions

    @Inject
    CustomerRepository customerRepo;

    @Inject
    TaxiRepository taxiRepo;

    @Inject
    BookingRepository bookingRepo;

    @POST
    @Transactional
    public Response create(@Valid GuestBookingCreate req) {
        // Check email uniqueness
        if (customerRepo.findByEmail(req.email) != null) {
            return Response.status(409).entity("Customer with this email already exists").build();
        }

        // Ensure taxi exists
        Taxi taxi = taxiRepo.findById(req.taxiId);
        if (taxi == null) {
            return Response.status(400).entity("Invalid taxiId").build();
        }

        // Prevent duplicate booking for taxi+date
        if (bookingRepo.findByTaxiAndDate(taxi, req.date) != null) {
            return Response.status(409).entity("Booking already exists for taxi and date").build();
        }

        Customer customer = new Customer();
        customer.setName(req.name);
        customer.setEmail(req.email);
        customer.setPhonenumber(req.phonenumber);
        em.persist(customer);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setTaxi(taxi);
        booking.setDate(req.date);
        em.persist(booking);

        return Response.created(URI.create("/api/bookings/" + booking.getId())).entity(booking).build();
    }
}