package com.example.taxi;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/customers/{id}/bookings")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerBookingsResource {

    @Inject
    BookingRepository bookingRepo;

    @GET
    public List<Booking> list(@PathParam("id") Long customerId) {
        return bookingRepo.listByCustomerId(customerId);
    }
}