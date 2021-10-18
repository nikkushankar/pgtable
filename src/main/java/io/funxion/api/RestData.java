package io.funxion.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("/tabledata")
public class RestData {
	@GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(@PathParam("param") String name) {
		//ResponseBuilder rb = new ResponseBuilder();
		return null;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createRecord(String input) {
        return null;
    }
}
