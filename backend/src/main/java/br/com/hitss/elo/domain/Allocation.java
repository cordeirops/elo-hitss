package br.com.hitss.elo.domain;

import java.time.LocalDate;

/** Vínculo entre um dev Hitss e um gestor do cliente, em um projeto. */
public class Allocation {
    public long id;
    public long developerId;
    public long clientManagerId;
    public String project;
    public LocalDate startDate;
    /** Primeiro dia em que o dev deixa de estar alocado. null enquanto a alocação estiver em andamento. */
    public LocalDate endDate;

    public boolean isActive(LocalDate today) {
        return endDate == null || endDate.isAfter(today);
    }
}
