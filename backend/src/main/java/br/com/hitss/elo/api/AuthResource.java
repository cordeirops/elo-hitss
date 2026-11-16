package br.com.hitss.elo.api;

import br.com.hitss.elo.domain.AppUser;
import br.com.hitss.elo.security.CurrentUser;
import br.com.hitss.elo.security.SessionRegistry;
import br.com.hitss.elo.service.EloService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    EloService service;

    @Inject
    SessionRegistry sessions;

    @Inject
    CurrentUser current;

    @POST
    @Path("/login")
    public Dto.LoginResponse login(Dto.LoginRequest request) {
        if (request == null) {
            throw new br.com.hitss.elo.service.ApiException(400, "Informe e-mail e senha.");
        }
        AppUser user = service.login(request.email(), request.password());
        return new Dto.LoginResponse(sessions.open(user.id), service.toView(user));
    }

    @POST
    @Path("/logout")
    public Response logout() {
        sessions.close(current.token());
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    public Dto.UserView me() {
        return service.toView(current.get());
    }
}
