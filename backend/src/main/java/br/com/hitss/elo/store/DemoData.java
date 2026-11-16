package br.com.hitss.elo.store;

import br.com.hitss.elo.domain.Allocation;
import br.com.hitss.elo.domain.AppUser;
import br.com.hitss.elo.domain.Developer;
import br.com.hitss.elo.domain.Feedback;
import br.com.hitss.elo.domain.Role;
import br.com.hitss.elo.service.EloService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Dados fictícios para demonstração. Nomes e empresas são inventados.
 * Os feedbacks são gerados relativos ao mês atual, então o painel sempre mostra histórico recente.
 */
@ApplicationScoped
public class DemoData {

    @Inject
    DataStore store;

    @Inject
    EloService service;

    void onStart(@Observes StartupEvent event) {
        // Usuários
        service.newUser("Marina Duarte", "gestor@hitss.demo", Role.HITSS_MANAGER, "Hitss", "hitss123");
        AppUser carlos = service.newUser("Carlos Menezes", "carlos@cliente.demo", Role.CLIENT_MANAGER,
                "Agência Exemplo de TI", "cliente123");
        AppUser patricia = service.newUser("Patrícia Lemos", "patricia@cliente.demo", Role.CLIENT_MANAGER,
                "Agência Exemplo de TI", "cliente123");

        // Devs
        Developer ana = dev("Ana Ribeiro", "ana.ribeiro@hitss.demo", "Dev Java Pleno", "Java 17, Quarkus, Kafka");
        Developer bruno = dev("Bruno Tavares", "bruno.tavares@hitss.demo", "Dev Java Sênior",
                "Java, WildFly, Oracle");
        Developer camila = dev("Camila Nogueira", "camila.nogueira@hitss.demo", "Dev Frontend Pleno",
                "Vue.js, Angular, TypeScript");
        Developer diego = dev("Diego Fontana", "diego.fontana@hitss.demo", "Dev Java Júnior",
                "Spring Boot, MongoDB");
        Developer elisa = dev("Elisa Barreto", "elisa.barreto@hitss.demo", "Analista DevOps",
                "Docker, Kubernetes, OpenShift");
        dev("Fábio Siqueira", "fabio.siqueira@hitss.demo", "Dev Java Pleno", "Java, Spring Boot");

        // Alocações
        LocalDate start = LocalDate.now().minusMonths(8);
        Allocation aAna = alloc(ana, carlos, "Portal do Cidadão", start);
        Allocation aBruno = alloc(bruno, carlos, "Portal do Cidadão", start.plusMonths(1));
        Allocation aCamila = alloc(camila, patricia, "App de Serviços", start.plusMonths(2));
        Allocation aDiego = alloc(diego, carlos, "Integrações Legadas", start.plusMonths(3));
        Allocation aElisa = alloc(elisa, patricia, "Plataforma OpenShift", start.plusMonths(2));

        // Histórico: períodos relativos ao mês atual (-3, -2, -1). Notas: técnica, comunicação, proatividade, prazo, equipe, recomendação
        YearMonth now = YearMonth.now();
        for (int back = 3; back >= 1; back--) {
            YearMonth p = now.minusMonths(back);
            int step = 3 - back; // 0, 1, 2 (evolução ao longo do tempo)
            // Ana: forte e constante
            fb(aAna, p, 5, 4, 5, 4, 5, 10, "Entrega com qualidade e antecipa riscos.",
                    step == 0 ? "Documentar melhor as decisões." : "", "Referência para o time.");
            // Bruno: caindo (para disparar alerta)
            int[] brunoTech = {5, 4, 4};
            int[] brunoComm = {4, 3, 3};
            int[] brunoDead = {4, 3, 2};
            fb(aBruno, p, brunoTech[step], brunoComm[step], 3, brunoDead[step], 4, step == 2 ? 6 : 8,
                    "Conhecimento técnico profundo.",
                    step == 2 ? "Atrasos recorrentes nas entregas e pouca comunicação sobre bloqueios." : "",
                    step == 2 ? "Precisa alinhar expectativas com o gestor." : "");
            // Camila: crescendo
            int[] camTech = {3, 4, 4};
            int[] camPro = {3, 4, 5};
            fb(aCamila, p, camTech[step], 5, camPro[step], 4, 5, step == 0 ? 8 : 9,
                    "Comunicação clara e boa relação com o negócio.", "", "Evoluiu muito no período.");
            // Diego: junior, regular
            fb(aDiego, p, 3, 4, 3, 4, 4, 7, "Aprende rápido e pergunta bem.",
                    "Ganhar autonomia em tarefas maiores.", "");
        }
        // Elisa: um feedback apenas, no mes passado
        fb(aElisa, now.minusMonths(1), 5, 5, 5, 5, 4, 10, "Automatizou os deploys e reduziu incidentes.", "",
                "Destaque da plataforma.");
        // Feedback do mes atual: apenas Ana e Camila (os demais ficam pendentes)
        fb(aAna, now, 5, 5, 5, 5, 5, 10, "Liderou a migração sem impacto ao usuário.", "", "");
        fb(aCamila, now, 4, 5, 5, 4, 5, 9, "Entregou o novo fluxo antes do prazo.", "", "");
    }

    private Developer dev(String name, String email, String title, String stack) {
        Developer d = new Developer();
        d.id = store.nextId();
        d.name = name;
        d.email = email;
        d.jobTitle = title;
        d.stack = stack;
        store.developers.put(d.id, d);
        return d;
    }

    private Allocation alloc(Developer d, AppUser manager, String project, LocalDate start) {
        Allocation a = new Allocation();
        a.id = store.nextId();
        a.developerId = d.id;
        a.clientManagerId = manager.id;
        a.project = project;
        a.startDate = start;
        store.allocations.put(a.id, a);
        return a;
    }

    private void fb(Allocation a, YearMonth period, int technical, int communication, int proactivity,
                    int commitment, int teamwork, int recommend, String strengths, String improvements,
                    String comment) {
        Feedback f = new Feedback();
        f.id = store.nextId();
        f.allocationId = a.id;
        f.developerId = a.developerId;
        f.clientManagerId = a.clientManagerId;
        f.period = period.toString();
        f.technical = technical;
        f.communication = communication;
        f.proactivity = proactivity;
        f.commitment = commitment;
        f.teamwork = teamwork;
        f.recommend = recommend;
        f.strengths = strengths;
        f.improvements = improvements;
        f.comment = comment;
        Instant when = period.atDay(Math.min(20, period.lengthOfMonth())).atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        if (when.isAfter(Instant.now())) {
            when = Instant.now();
        }
        f.createdAt = when;
        f.updatedAt = when;
        store.feedbacks.put(f.id, f);
    }
}
