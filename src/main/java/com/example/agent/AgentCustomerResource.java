package com.example.agent;

import com.example.agent.client.FlightClient;
import com.example.agent.client.HotelClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/agent/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentCustomerResource {

    @Inject
    AgentCustomerRepository customers;

    @Inject
    AgentCustomerMappingRepository mappings;

    @Inject
    com.example.taxi.TaxiOperations taxiOps;

    @Inject
    @org.eclipse.microprofile.rest.client.inject.RestClient
    HotelClient hotelClient;

    @Inject
    @org.eclipse.microprofile.rest.client.inject.RestClient
    FlightClient flightClient;

    @GET
    @Operation(summary = "List agent customers")
    public List<AgentCustomer> list() {
        return customers.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get agent customer with downstream mapping")
    public Response get(@PathParam("id") Long id) {
        var c = customers.findById(id);
        if (c == null) return Response.status(Response.Status.NOT_FOUND).build();
        Map<String, Object> out = new HashMap<>();
        out.put("customer", c);
        var m = mappings.findByAgentCustomerId(id);
        if (m != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("taxiCustomerId", m.getTaxiCustomerId());
            map.put("hotelCustomerId", m.getHotelCustomerId());
            map.put("flightCustomerId", m.getFlightCustomerId());
            out.put("mapping", map);
        }
        return Response.ok(out).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Create agent-level customer and propagate to services")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "409", description = "Duplicate email at agent or downstream")
    public Response create(@Valid AgentCustomerCreate req) {
        // Agent uniqueness by email
        if (customers.findByEmail(req.email) != null) {
            return Response.status(409).entity("Customer with this email already exists at agent").build();
        }

        AgentCustomer c = new AgentCustomer();
        c.setName(req.name);
        c.setEmail(req.email);
        c.setPhonenumber(req.phonenumber);
        customers.persist(c);

        // Propagate to downstream services
        var taxiCustomer = taxiOps.ensureCustomerByEmail(req.name, req.email, req.phonenumber);
        Long taxiId = taxiCustomer != null ? taxiCustomer.getId() : null;
        Long hotelId = propagateCreateCustomer(hotelClient, req);
        Long flightId = propagateCreateCustomer(flightClient, req);

        AgentCustomerMapping m = new AgentCustomerMapping();
        m.setAgentCustomerId(c.getId());
        m.setTaxiCustomerId(taxiId);
        m.setHotelCustomerId(hotelId);
        m.setFlightCustomerId(flightId);
        mappings.persist(m);

        Map<String, Object> out = new HashMap<>();
        out.put("id", c.getId());
        out.put("name", c.getName());
        out.put("email", c.getEmail());
        out.put("phonenumber", c.getPhonenumber());
        Map<String, Object> map = new HashMap<>();
        map.put("taxiCustomerId", taxiId);
        map.put("hotelCustomerId", hotelId);
        map.put("flightCustomerId", flightId);
        out.put("mapping", map);

        return Response.created(URI.create("/api/agent/customers/" + c.getId())).entity(out).build();
    }

    private Long propagateCreateCustomer(Object client, AgentCustomerCreate req) {
        DownstreamCustomerCreate dto = new DownstreamCustomerCreate(req.name, req.email, req.phonenumber);
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
            // On 409 duplicate, try to find existing by email
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
                        .filter(x -> req.email.equalsIgnoreCase(x.email))
                        .map(x -> x.id)
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}