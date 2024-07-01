package org.acme.hibernate.orm.panache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/fruits")
@ApplicationScoped
public class FruitResource {

    private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());

    @GET
    public Uni<List<Fruit>> get() {
        return Fruit.listAll(Sort.by("name"));
    }

    @Path("/{id}")
    @GET
    public Uni<Fruit> getSingle(Long id) {
        return Fruit.findById(id);
    }

    @Path("/{id}")
    @PUT
    public Uni<Response> update(Fruit fruit, Long id) {
        if (fruit == null || fruit.name == null) {
            throw new WebApplicationException("Fruit name was not set on request", 422);
        }
        return Panache
                .withTransaction(() -> Fruit.<Fruit> findById(id)
                        .onItem().ifNotNull().invoke(entity -> entity.name = fruit.name)
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);
    }

    @Path("/{id}")
    @DELETE
    public Uni<Response> delete(Long id) {
        return Panache
                .withTransaction(() -> Fruit.deleteById(id))
                .map(deleted -> deleted
                    ? Response.ok().status(Response.Status.NO_CONTENT).build()
                    : Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<RestResponse<Fruit>> create(Fruit fruit) {
        if (fruit == null || fruit.id != null) {
            throw new WebApplicationException("Fruit name was invalidly set on request", 422);
        }

        return Panache.withTransaction(fruit::persist).replaceWith(RestResponse.status(RestResponse.Status.CREATED, fruit));
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            Throwable throwable = exception;

            int code = 500;
            if (throwable instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            if (throwable instanceof CompositeException) {
                throwable = ((CompositeException) throwable).getCause();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", throwable.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", throwable.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }
    }
}
