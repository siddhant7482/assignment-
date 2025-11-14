package com.example.agent.client;

import com.example.agent.BookingResult;
import com.example.agent.FlightBookingCreate;
import com.example.agent.FlightBookingDto;
import com.example.agent.CustomerResult;
import com.example.agent.DownstreamCustomerCreate;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@RegisterRestClient(configKey = "flight-api")
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface FlightClient {

    @POST
    @Path("/bookings")
    BookingResult createBooking(FlightBookingCreate request);

    @DELETE
    @Path("/bookings/{id}")
    Response cancelBooking(@PathParam("id") Long id);

    @GET
    @Path("/bookings")
    List<FlightBookingDto> listBookings(@QueryParam("customerId") Long customerId);

    @POST
    @Path("/customers")
    CustomerResult createCustomer(DownstreamCustomerCreate request);

    @GET
    @Path("/customers")
    List<CustomerResult> listCustomers();
}