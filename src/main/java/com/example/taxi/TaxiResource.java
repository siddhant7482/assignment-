package com.example.taxi;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/taxis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaxiResource {

    @Inject
    TaxiRepository repo;

    @GET
    public List<Taxi> list() {
        return repo.listAll();
    }

    @POST
    public Response create(@Valid TaxiCreate req) {
        if (repo.findByRegistration(req.registration) != null) {
            return Response.status(409).entity("Taxi with this registration already exists").build();
        }
        Taxi t = new Taxi();
        t.setRegistration(req.registration);
        t.setSeats(req.seats);
        repo.create(t);
        return Response.created(URI.create("/api/taxis/" + t.getId())).entity(t).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = repo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }
}