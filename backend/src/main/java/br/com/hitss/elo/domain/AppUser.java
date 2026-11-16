package br.com.hitss.elo.domain;

/** Usuario que faz login (gestor Hitss ou gestor do cliente). Nunca e serializado direto na API. */
public class AppUser {
    public long id;
    public String name;
    public String email;
    public Role role;
    /** "Hitss" para gestores Hitss, nome do cliente para gestores do cliente. */
    public String organization;
    public String salt;
    public String passwordHash;
    public boolean active = true;
}
