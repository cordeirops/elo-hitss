package br.com.hitss.elo.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tokens de sessão assinados (HMAC-SHA256), no formato {@code userId.expiraEmEpochSegundos.assinatura}.
 * Não guardam estado no servidor, então qualquer instância do backend valida o token de outra
 * (necessário quando a plataforma sobe mais de uma instância). Defina ELO_SESSION_SECRET em produção.
 */
@ApplicationScoped
public class SessionRegistry {

    @ConfigProperty(name = "elo.session.hours", defaultValue = "8")
    long sessionHours;

    @ConfigProperty(name = "elo.session.secret", defaultValue = "elo-demo-secret-troque-em-producao")
    String secret;

    public String open(long userId) {
        long exp = Instant.now().plus(Duration.ofHours(sessionHours)).getEpochSecond();
        String payload = userId + "." + exp;
        return payload + "." + sign(payload);
    }

    /** Retorna o id do usuário dono do token, ou null se inválido/expirado. */
    public Long resolve(String token) {
        if (token == null) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String payload = parts[0] + "." + parts[1];
        byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, parts[2].getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            long exp = Long.parseLong(parts[1]);
            if (exp < Instant.now().getEpochSecond()) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Token sem estado: o logout é feito descartando o token no navegador. */
    public void close(String token) {
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
