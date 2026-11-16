package br.com.hitss.elo.api;

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

/** Área do gestor Hitss (o AuthFilter exige o perfil HITSS_MANAGER para api/hitss/**). */
@Path("/api/hitss")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HitssResource {

    @Inject
    EloService service;

    @GET
    @Path("/dashboard")
    public Dto.DashboardView dashboard() {
        return service.dashboard();
    }

    // ---- Devs

    @GET
    @Path("/developers")
    public List<Dto.DeveloperView> developers() {
        return service.listDevelopers();
    }

    @POST
    @Path("/developers")
    public Response createDeveloper(Dto.DeveloperRequest request) {
        Dto.DeveloperView created = service.createDeveloper(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/developers/{id}")
    public Dto.DeveloperDetailView developer(@PathParam("id") long id) {
        return service.developerDetail(id);
    }

    @PUT
    @Path("/developers/{id}")
    public Dto.DeveloperView updateDeveloper(@PathParam("id") long id, Dto.DeveloperRequest request) {
        return service.updateDeveloper(id, request);
    }

    // ---- Gestores do cliente

    @GET
    @Path("/client-managers")
    public List<Dto.ClientManagerView> clientManagers() {
        return service.listClientManagers();
    }

    @POST
    @Path("/client-managers")
    public Response createClientManager(Dto.ClientManagerRequest request) {
        Dto.ClientManagerView created = service.createClientManager(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // ---- Alocações

    @GET
    @Path("/allocations")
    public List<Dto.AllocationView> allocations(@QueryParam("onlyActive") boolean onlyActive) {
        return service.listAllocations(onlyActive);
    }

    @POST
    @Path("/allocations")
    public Response createAllocation(Dto.AllocationRequest request) {
        Dto.AllocationView created = service.createAllocation(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/allocations/{id}/end")
    public Dto.AllocationView endAllocation(@PathParam("id") long id) {
        return service.endAllocation(id);
    }

    // ---- Feedbacks (somente leitura para a Hitss)

    @GET
    @Path("/feedbacks")
    public List<Dto.FeedbackView> feedbacks(@QueryParam("developerId") Long developerId,
                                            @QueryParam("period") String period,
                                            @QueryParam("clientManagerId") Long clientManagerId) {
        return service.listFeedbacks(developerId, period, clientManagerId);
    }
}
