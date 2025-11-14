package com.example.agent.client;

import com.example.agent.BookingResult;
import com.example.agent.TaxiBookingCreate;
import com.example.agent.TaxiBookingDto;
import com.example.agent.CustomerResult;
import com.example.agent.DownstreamCustomerCreate;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@RegisterRestClient(configKey = "taxi-api")
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TaxiClient {

    @POST
    @Path("/bookings")
    BookingResult createBooking(TaxiBookingCreate request);

    @DELETE
    @Path("/bookings/{id}")
    Response cancelBooking(@PathParam("id") Long id);

    @GET
    @Path("/customers/{id}/bookings")
    List<TaxiBookingDto> listCustomerBookings(@PathParam("id") Long id);

    @POST
    @Path("/customers")
    CustomerResult createCustomer(DownstreamCustomerCreate request);

    @GET
    @Path("/customers")
    List<CustomerResult> listCustomers();
}