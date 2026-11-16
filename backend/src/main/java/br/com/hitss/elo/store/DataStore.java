package br.com.hitss.elo.store;

import br.com.hitss.elo.domain.Allocation;
import br.com.hitss.elo.domain.AppUser;
import br.com.hitss.elo.domain.Developer;
import br.com.hitss.elo.domain.Feedback;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Armazenamento em memória, suficiente para o protótipo.
 * Em producao isto vira Hibernate ORM com Panache + PostgreSQL (as entidades de domino ja separam o modelo da API).
 */
@ApplicationScoped
public class DataStore {
    public final Map<Long, AppUser> users = new ConcurrentHashMap<>();
    public final Map<Long, Developer> developers = new ConcurrentHashMap<>();
    public final Map<Long, Allocation> allocations = new ConcurrentHashMap<>();
    public final Map<Long, Feedback> feedbacks = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong(0);

    public long nextId() {
        return sequence.incrementAndGet();
    }
}
