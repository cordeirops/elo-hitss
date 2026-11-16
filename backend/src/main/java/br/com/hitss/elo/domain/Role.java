package br.com.hitss.elo.domain;

/** Perfis de acesso do protótipo. */
public enum Role {
    /** Gestor da Hitss: cadastra devs, aloca aos gestores do cliente e le os feedbacks. */
    HITSS_MANAGER,
    /** Gestor do cliente: avalia os profissionais alocados a ele. */
    CLIENT_MANAGER
}
