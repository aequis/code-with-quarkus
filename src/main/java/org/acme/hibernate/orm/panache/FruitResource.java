package org.acme.hibernate.orm.panache;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/fruits")
@ApplicationScoped
public class FruitResource {

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

    @POST
    public Uni<RestResponse<Fruit>> create(Fruit fruit) {
        return Panache.withTransaction(fruit::persist).replaceWith(RestResponse.status(RestResponse.Status.CREATED, fruit));
    }
}
