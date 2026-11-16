package br.com.hitss.elo.domain;

import java.time.Instant;

/** Feedback mensal dado por um gestor do cliente sobre um profissional alocado. */
public class Feedback {
    public long id;
    public long allocationId;
    public long developerId;
    public long clientManagerId;
    /** Competencia no formato yyyy-MM. Um feedback por alocação por competência. */
    public String period;

    // Critérios de 1 a 5
    public int technical;
    public int communication;
    public int proactivity;
    public int commitment;
    public int teamwork;
    // Recomendação de 0 a 10 (base do NPS do profissional)
    public int recommend;

    public String strengths;
    public String improvements;
    public String comment;

    public Instant createdAt;
    public Instant updatedAt;

    /** Media simples dos cinco critérios. */
    public double score() {
        return (technical + communication + proactivity + commitment + teamwork) / 5.0;
    }
}
