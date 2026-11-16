package br.com.hitss.elo.api;

import br.com.hitss.elo.security.CurrentUser;
import br.com.hitss.elo.service.EloService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/** Área do gestor do cliente (o AuthFilter exige o perfil CLIENT_MANAGER para api/client/**). */
@Path("/api/client")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

    @Inject
    EloService service;

    @Inject
    CurrentUser current;

    /** Profissionais alocados a este gestor, com o status do feedback da competência atual. */
    @GET
    @Path("/team")
    public Dto.TeamView team() {
        return service.team(current.get());
    }

    @GET
    @Path("/feedbacks")
    public List<Dto.FeedbackView> feedbacks(@QueryParam("developerId") Long developerId) {
        return service.myFeedbacks(current.get(), developerId);
    }

    @GET
    @Path("/feedbacks/{id}")
    public Dto.FeedbackView feedback(@PathParam("id") long id) {
        return service.myFeedback(current.get(), id);
    }

    @POST
    @Path("/feedbacks")
    public Response create(Dto.FeedbackRequest request) {
        Dto.FeedbackView created = service.createFeedback(current.get(), request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/feedbacks/{id}")
    public Dto.FeedbackView update(@PathParam("id") long id, Dto.FeedbackRequest request) {
        return service.updateFeedback(current.get(), id, request);
    }
}
