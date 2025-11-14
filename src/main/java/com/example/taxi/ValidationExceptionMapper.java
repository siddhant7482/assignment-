package com.example.taxi;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> v : exception.getConstraintViolations()) {
            String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
            errors.put(field, v.getMessage());
        }
        return Response.status(422).entity(errors).build();
    }
}