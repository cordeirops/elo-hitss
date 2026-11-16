package br.com.hitss.elo.security;

import br.com.hitss.elo.api.Dto;
import br.com.hitss.elo.domain.AppUser;
import br.com.hitss.elo.domain.Role;
import br.com.hitss.elo.store.DataStore;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Autenticação por token (Authorization: Bearer ...) e autorização por perfil, pelo prefixo da rota:
 * <ul>
 *   <li>api/auth/login: público</li>
 *   <li>api/hitss/**: somente HITSS_MANAGER</li>
 *   <li>api/client/**: somente CLIENT_MANAGER</li>
 *   <li>demais rotas em api/: qualquer usuário autenticado</li>
 * </ul>
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    SessionRegistry sessions;

    @Inject
    DataStore store;

    @Inject
    CurrentUser current;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.startsWith("api/") || path.equals("api/auth/login")) {
            return;
        }

        String header = ctx.getHeaderString("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring("Bearer ".length()).trim();
        }

        Long userId = sessions.resolve(token);
        AppUser user = userId == null ? null : store.users.get(userId);
        if (user == null || !user.active) {
            ctx.abortWith(error(401, "Sessão inválida ou expirada. Faça login novamente."));
            return;
        }

        if (path.startsWith("api/hitss/") && user.role != Role.HITSS_MANAGER) {
            ctx.abortWith(error(403, "Acesso restrito a gestores Hitss."));
            return;
        }
        if (path.startsWith("api/client/") && user.role != Role.CLIENT_MANAGER) {
            ctx.abortWith(error(403, "Acesso restrito a gestores do cliente."));
            return;
        }

        current.set(user, token);
    }

    private Response error(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new Dto.ErrorBody(message))
                .build();
    }
}
