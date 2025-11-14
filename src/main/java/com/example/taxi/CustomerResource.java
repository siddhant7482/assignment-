package com.example.taxi;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerRepository repo;

    @GET
    public List<Customer> list() {
        return repo.listAll();
    }

    @POST
    public Response create(@Valid CustomerCreate req) {
        // uniqueness check
        if (repo.findByEmail(req.email) != null) {
            return Response.status(409).entity("Customer with this email already exists").build();
        }

        Customer c = new Customer();
        c.setName(req.name);
        c.setEmail(req.email);
        c.setPhonenumber(req.phonenumber);
        repo.create(c);

        return Response.created(URI.create("/api/customers/" + c.getId())).entity(c).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = repo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }
}