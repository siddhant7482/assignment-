package com.example.agent;

import com.example.agent.client.FlightClient;
import com.example.agent.client.HotelClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.taxi.TaxiOperations;
import com.example.taxi.Booking;

@Path("/api/agent/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelAgentResource {

    @Inject
    TravelAgentBookingRepository repository;

    @Inject
    AgentCustomerRepository agentCustomers;

    @Inject
    AgentCustomerMappingRepository customerMappings;



    @Inject
    @RestClient
    HotelClient hotelClient;

    @Inject
    @RestClient
    FlightClient flightClient;

    @Inject
    TaxiOperations taxiOps;

    @GET
    @Operation(summary = "List aggregate bookings")
    @APIResponse(responseCode = "200", description = "List of bookings")
    public List<TravelAgentBooking> list(@QueryParam("customerId") Long customerId) {
        if (customerId != null) {
            return repository.findByCustomerId(customerId);
        }
        return repository.findAll();
    }

    @POST
    @Transactional
    @Operation(summary = "Create an aggregate booking across Taxi, Hotel, Flight")
    @APIResponse(responseCode = "201", description = "Aggregate booking created")
    @APIResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = Map.class)))
    @APIResponse(responseCode = "409", description = "Conflict from downstream service")
    @APIResponse(responseCode = "422", description = "Unprocessable entity from downstream service")
    public Response create(@Valid TravelAgentRequest req) {
        Map<String, Object> error = new HashMap<>();
        String validation = validate(req);
        if (validation != null) {
            error.put("message", validation);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Resolve downstream IDs from mapping
        Long taxiCustomerId = null;
        Long hotelCustomerId = null;
        Long flightCustomerId = null;
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(req.customerId);
        if (map != null) {
            taxiCustomerId = map.getTaxiCustomerId();
            hotelCustomerId = map.getHotelCustomerId();
            flightCustomerId = map.getFlightCustomerId();
        } else {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Customer must be created via agent before booking. Create at /api/agent/customers then retry.");
            details.put("customerId", req.customerId);
            return Response.status(422).entity(details).build();
        }

        // Require downstream IDs before booking
        if (taxiCustomerId == null || hotelCustomerId == null || flightCustomerId == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers missing");
            List<String> missing = new ArrayList<>();
            if (taxiCustomerId == null) missing.add("taxi");
            if (hotelCustomerId == null) missing.add("hotel");
            if (flightCustomerId == null) missing.add("flight");
            details.put("missing", missing);
            // Return 422
            return Response.status(422).entity(details).build();
        }

        Long taxiBookingId = null;
        Long hotelBookingId = null;
        Long flightBookingId = null;

        try {
            // Taxi booking
            Booking taxiBooking = taxiOps.createTaxiBooking(taxiCustomerId, req.taxiId, req.date);
            taxiBookingId = taxiBooking != null ? taxiBooking.getId() : null;

            // Hotel booking
            BookingResult hotelResult = hotelClient.createBooking(new HotelBookingCreate(hotelCustomerId, req.hotelId, req.date));
            hotelBookingId = hotelResult != null ? hotelResult.id : null;

            // Flight booking
            BookingResult flightResult = flightClient.createBooking(new FlightBookingCreate(flightCustomerId, req.flightId, req.date));
            flightBookingId = flightResult != null ? flightResult.id : null;

            // Persist aggregate booking
            TravelAgentBooking agg = new TravelAgentBooking();
            agg.setCustomerId(req.customerId);
            agg.setDate(req.date);
            agg.setTaxiBookingId(taxiBookingId);
            agg.setHotelBookingId(hotelBookingId);
            agg.setFlightBookingId(flightBookingId);
            repository.persist(agg);

            return Response.status(Response.Status.CREATED).entity(agg).build();

        } catch (WebApplicationException wae) {
            // Compensate downstream error
            if (hotelBookingId != null) {
                safeCancelHotel(hotelBookingId);
            }
            if (taxiBookingId != null) {
                safeCancelTaxi(taxiBookingId);
            }
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Downstream error: " + wae.getMessage());
            // Identify failing service
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(wae.getResponse().getStatus()).entity(downstream).build();
        } catch (Exception e) {
            // Compensate and return 502
            if (flightBookingId != null) {
                safeCancelFlight(flightBookingId);
            }
            if (hotelBookingId != null) {
                safeCancelHotel(hotelBookingId);
            }
            if (taxiBookingId != null) {
                safeCancelTaxi(taxiBookingId);
            }
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Agent orchestration error: " + e.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(Response.Status.BAD_GATEWAY).entity(downstream).build();
        }
    }

    @POST
    @Path("/guest")
    @Transactional
    @Operation(summary = "Guest aggregate booking: create customer + book across services atomically")
    @APIResponse(responseCode = "201", description = "Guest aggregate booking created")
    @APIResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = Map.class)))
    @APIResponse(responseCode = "409", description = "Conflict from downstream service")
    @APIResponse(responseCode = "422", description = "Unprocessable entity from downstream service")
    public Response createGuest(@Valid GuestBookingRequest req) {
        Map<String, Object> error = new HashMap<>();
        String validation = validateGuest(req);
        if (validation != null) {
            error.put("message", validation);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Find or create agent customer
        AgentCustomer agentCustomer = agentCustomers.findByEmail(req.email);
        boolean newlyCreatedAgentCustomer = false;
        if (agentCustomer == null) {
            agentCustomer = new AgentCustomer();
            agentCustomer.setName(req.name);
            agentCustomer.setEmail(req.email);
            agentCustomer.setPhonenumber(req.phonenumber);
            agentCustomers.persist(agentCustomer);
            newlyCreatedAgentCustomer = true;
        }

        // Init downstream mappings
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(agentCustomer.getId());
        if (map == null) {
            map = new AgentCustomerMapping();
            map.setAgentCustomerId(agentCustomer.getId());
        }

        if (map.getTaxiCustomerId() == null) {
            var taxiCustomer = taxiOps.ensureCustomerByEmail(req.name, req.email, req.phonenumber);
            map.setTaxiCustomerId(taxiCustomer != null ? taxiCustomer.getId() : null);
        }
        if (map.getHotelCustomerId() == null) {
            Long id = propagateGuestCustomer(hotelClient, req.name, req.email, req.phonenumber);
            map.setHotelCustomerId(id);
        }
        if (map.getFlightCustomerId() == null) {
            Long id = propagateGuestCustomer(flightClient, req.name, req.email, req.phonenumber);
            map.setFlightCustomerId(id);
        }

        // Persist mapping
        if (map.getId() == null) {
            customerMappings.persist(map);
        }

        // Require downstream IDs
        if (map.getTaxiCustomerId() == null || map.getHotelCustomerId() == null || map.getFlightCustomerId() == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers missing");
            List<String> missing = new ArrayList<>();
            if (map.getTaxiCustomerId() == null) missing.add("taxi");
            if (map.getHotelCustomerId() == null) missing.add("hotel");
            if (map.getFlightCustomerId() == null) missing.add("flight");
            details.put("missing", missing);
            return Response.status(422).entity(details).build();
        }

        Long taxiBookingId = null;
        Long hotelBookingId = null;
        Long flightBookingId = null;

        try {
            // Taxi booking
            Booking taxiBooking = taxiOps.createTaxiBooking(map.getTaxiCustomerId(), req.taxiId, req.date);
            taxiBookingId = taxiBooking != null ? taxiBooking.getId() : null;

            // Hotel booking
            BookingResult hotelResult = hotelClient.createBooking(new HotelBookingCreate(map.getHotelCustomerId(), req.hotelId, req.date));
            hotelBookingId = hotelResult != null ? hotelResult.id : null;

            // Flight booking
            BookingResult flightResult = flightClient.createBooking(new FlightBookingCreate(map.getFlightCustomerId(), req.flightId, req.date));
            flightBookingId = flightResult != null ? flightResult.id : null;

            // Persist aggregate booking
            TravelAgentBooking agg = new TravelAgentBooking();
            agg.setCustomerId(agentCustomer.getId());
            agg.setDate(req.date);
            agg.setTaxiBookingId(taxiBookingId);
            agg.setHotelBookingId(hotelBookingId);
            agg.setFlightBookingId(flightBookingId);
            repository.persist(agg);

            return Response.status(Response.Status.CREATED).entity(agg).build();

        } catch (WebApplicationException wae) {
            if (hotelBookingId != null) {
                safeCancelHotel(hotelBookingId);
            }
            if (taxiBookingId != null) {
                safeCancelTaxi(taxiBookingId);
            }
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Downstream error: " + wae.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(wae.getResponse().getStatus()).entity(downstream).build();
        } catch (Exception e) {
            if (flightBookingId != null) {
                safeCancelFlight(flightBookingId);
            }
            if (hotelBookingId != null) {
                safeCancelHotel(hotelBookingId);
            }
            if (taxiBookingId != null) {
                safeCancelTaxi(taxiBookingId);
            }
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Agent orchestration error: " + e.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(Response.Status.BAD_GATEWAY).entity(downstream).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Cancel an aggregate booking and downstream bookings")
    @APIResponse(responseCode = "204", description = "Cancelled")
    @APIResponse(responseCode = "404", description = "Not found")
    public Response cancel(@PathParam("id") Long id) {
        TravelAgentBooking agg = repository.findById(id);
        if (agg == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        safeCancelFlight(agg.getFlightBookingId());
        safeCancelHotel(agg.getHotelBookingId());
        safeCancelTaxi(agg.getTaxiBookingId());
        repository.delete(agg);
        return Response.noContent().build();
    }

    @GET
    @Path("/customers/{id}/bookings")
    @Operation(summary = "Live consolidated bookings across Taxi, Hotel, Flight for a customer")
    @APIResponse(responseCode = "200", description = "Aggregated bookings")
    public Response liveCustomerBookings(@PathParam("id") Long customerId) {
        CustomerBookingsAggregate agg = new CustomerBookingsAggregate();
        agg.customerId = customerId;

        Map<String, String> errors = new HashMap<>();

        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(customerId);
        if (map == null) {
            errors.put("mapping", "No downstream mapping for agent customerId");
            agg.errors = errors;
            // Return empty lists
            agg.taxi = Collections.emptyList();
            agg.hotel = Collections.emptyList();
            agg.flight = Collections.emptyList();
            return Response.ok(agg).build();
        }

        try {
            var bookings = taxiOps.listTaxiBookingsForCustomer(map.getTaxiCustomerId());
            agg.taxi = bookings.stream().map(this::toTaxiDto).toList();
        } catch (WebApplicationException wae) {
            errors.put("taxi", "Downstream error: " + wae.getMessage());
        } catch (Exception e) {
            errors.put("taxi", "Unexpected error: " + e.getMessage());
        }

        try {
            agg.hotel = hotelClient.listBookings(map.getHotelCustomerId());
        } catch (WebApplicationException wae) {
            errors.put("hotel", "Downstream error: " + wae.getMessage());
        } catch (Exception e) {
            errors.put("hotel", "Unexpected error: " + e.getMessage());
        }

        try {
            agg.flight = flightClient.listBookings(map.getFlightCustomerId());
        } catch (WebApplicationException wae) {
            errors.put("flight", "Downstream error: " + wae.getMessage());
        } catch (Exception e) {
            errors.put("flight", "Unexpected error: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            agg.errors = errors;
        }

        return Response.ok(agg).build();
    }

    private String validate(TravelAgentRequest req) {
        if (req == null) return "Request body is required";
        if (req.customerId == null || req.customerId <= 0) return "customerId must be positive";
        if (req.taxiId == null || req.taxiId <= 0) return "taxiId must be positive";
        if (req.hotelId == null || req.hotelId <= 0) return "hotelId must be positive";
        if (req.flightId == null || req.flightId <= 0) return "flightId must be positive";
        if (req.date == null) return "date must be provided";
        if (!req.date.isAfter(LocalDate.now())) return "date must be in the future";
        return null;
    }

    private String validateGuest(GuestBookingRequest req) {
        if (req == null) return "Request body is required";
        if (req.name == null || req.name.trim().isEmpty()) return "name must be provided";
        if (req.email == null || req.email.trim().isEmpty()) return "email must be provided";
        if (req.phonenumber == null || req.phonenumber.trim().isEmpty()) return "phonenumber must be provided";
        if (req.taxiId == null || req.taxiId <= 0) return "taxiId must be positive";
        if (req.hotelId == null || req.hotelId <= 0) return "hotelId must be positive";
        if (req.flightId == null || req.flightId <= 0) return "flightId must be positive";
        if (req.date == null) return "date must be provided";
        if (!req.date.isAfter(LocalDate.now())) return "date must be in the future";
        return null;
    }

    private void safeCancelTaxi(Long id) {
        if (id == null) return;
        try { taxiOps.cancelTaxiBooking(id); } catch (Exception ignored) {}
    }

    private void safeCancelHotel(Long id) {
        if (id == null) return;
        try { hotelClient.cancelBooking(id); } catch (Exception ignored) {}
    }

    private void safeCancelFlight(Long id) {
        if (id == null) return;
        try { flightClient.cancelBooking(id); } catch (Exception ignored) {}
    }

    private String inferFailedService(Long taxiBookingId, Long hotelBookingId, Long flightBookingId) {
        // Infer failing service
        if (taxiBookingId == null) return "taxi";
        if (hotelBookingId == null) return "hotel";
        if (flightBookingId == null) return "flight";
        return "unknown";
    }

    private Long propagateGuestCustomer(Object client, String name, String email, String phonenumber) {
        DownstreamCustomerCreate dto = new DownstreamCustomerCreate(name, email, phonenumber);
        try {
            CustomerResult created;
            if (client instanceof HotelClient hc) {
                created = hc.createCustomer(dto);
            } else if (client instanceof FlightClient fc) {
                created = fc.createCustomer(dto);
            } else {
                return null;
            }
            return created != null ? created.id : null;
        } catch (jakarta.ws.rs.WebApplicationException wae) {
            if (wae.getResponse().getStatus() == 409) {
                List<CustomerResult> list;
                if (client instanceof HotelClient hc) {
                    list = hc.listCustomers();
                } else if (client instanceof FlightClient fc) {
                    list = fc.listCustomers();
                } else {
                    return null;
                }
                return list.stream()
                        .filter(x -> email.equalsIgnoreCase(x.email))
                        .map(x -> x.id)
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private TaxiBookingDto toTaxiDto(com.example.taxi.Booking b) {
        TaxiBookingDto dto = new TaxiBookingDto();
        dto.id = b.getId();
        dto.date = b.getDate();
        Map<String, Object> taxiInfo = new HashMap<>();
        taxiInfo.put("id", b.getTaxi().getId());
        taxiInfo.put("registration", b.getTaxi().getRegistration());
        taxiInfo.put("seats", b.getTaxi().getSeats());
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("id", b.getCustomer().getId());
        customerInfo.put("name", b.getCustomer().getName());
        customerInfo.put("email", b.getCustomer().getEmail());
        customerInfo.put("phonenumber", b.getCustomer().getPhonenumber());
        dto.taxi = taxiInfo;
        dto.customer = customerInfo;
        return dto;
    }
}