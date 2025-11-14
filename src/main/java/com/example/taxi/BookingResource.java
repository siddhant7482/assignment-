package com.example.taxi;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    BookingRepository bookingRepo;

    @Inject
    CustomerRepository customerRepo;

    @Inject
    TaxiRepository taxiRepo;

    @GET
    public List<Booking> list() {
        return bookingRepo.listAll();
    }

    // Removed GET by id to avoid duplicate GET in Swagger UI

    @POST
    @Operation(hidden = true)
    public Response create(@Valid BookingCreate req) {
        var customer = customerRepo.findById(req.customerId);
        if (customer == null) {
            return Response.status(400).entity("Invalid customerId").build();
        }

        var taxi = taxiRepo.findById(req.taxiId);
        if (taxi == null) {
            return Response.status(400).entity("Invalid taxiId").build();
        }

        if (bookingRepo.findByTaxiAndDate(taxi, req.date) != null) {
            return Response.status(409).entity("Booking already exists for taxi and date").build();
        }

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setTaxi(taxi);
        b.setDate(req.date);
        bookingRepo.create(b);

        return Response.created(URI.create("/api/bookings/" + b.getId())).entity(b).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = bookingRepo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/delete")
    public Response deletePost(@PathParam("id") Long id) {
        boolean deleted = bookingRepo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }
}