package br.com.hitss.elo.security;

import br.com.hitss.elo.domain.AppUser;
import jakarta.enterprise.context.RequestScoped;

/** Usuario autenticado da requisição atual (preenchido pelo AuthFilter). */
@RequestScoped
public class CurrentUser {
    private AppUser user;
    private String token;

    public AppUser get() {
        return user;
    }

    public String token() {
        return token;
    }

    public void set(AppUser user, String token) {
        this.user = user;
        this.token = token;
    }
}
