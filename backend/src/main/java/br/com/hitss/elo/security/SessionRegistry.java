package br.com.hitss.elo.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sessoes (token opaco -> usuário) mantidas em memória. */
@ApplicationScoped
public class SessionRegistry {

    private record Session(long userId, Instant expiresAt) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @ConfigProperty(name = "elo.session.hours", defaultValue = "8")
    long sessionHours;

    public String open(long userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(userId, Instant.now().plus(Duration.ofHours(sessionHours))));
        return token;
    }

    /** Retorna o id do usuário dono do token, ou null se inválido/expirado. */
    public Long resolve(String token) {
        if (token == null) {
            return null;
        }
        Session s = sessions.get(token);
        if (s == null) {
            return null;
        }
        if (s.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return s.userId();
    }

    public void close(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
