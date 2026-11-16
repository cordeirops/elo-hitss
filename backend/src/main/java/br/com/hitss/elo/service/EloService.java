package br.com.hitss.elo.service;

import br.com.hitss.elo.api.Dto.*;
import br.com.hitss.elo.domain.Allocation;
import br.com.hitss.elo.domain.AppUser;
import br.com.hitss.elo.domain.Developer;
import br.com.hitss.elo.domain.Feedback;
import br.com.hitss.elo.domain.Role;
import br.com.hitss.elo.security.PasswordHasher;
import br.com.hitss.elo.store.DataStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Regras de negócio do Elo. Resources ficam finos e delegam tudo para cá. */
@ApplicationScoped
public class EloService {

    private static final int MAX_TEXT = 2000;

    @Inject
    DataStore store;

    @Inject
    PasswordHasher hasher;

    // ------------------------------------------------------------------ helpers

    public String currentPeriod() {
        return YearMonth.now().toString();
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String text(String s, String field) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.length() > MAX_TEXT) {
            throw new ApiException(400, "O campo " + field + " aceita no máximo " + MAX_TEXT + " caracteres.");
        }
        return t;
    }

    public UserView toView(AppUser u) {
        return new UserView(u.id, u.name, u.email, u.role, u.organization);
    }

    private AppUser managerOf(long id) {
        return store.users.get(id);
    }

    // ------------------------------------------------------------------ auth

    public AppUser login(String email, String password) {
        if (blank(email) || blank(password)) {
            throw new ApiException(400, "Informe e-mail e senha.");
        }
        AppUser user = store.users.values().stream()
                .filter(u -> u.active && u.email.equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElse(null);
        if (user == null || !hasher.matches(password, user.salt, user.passwordHash)) {
            throw new ApiException(401, "E-mail ou senha inválidos.");
        }
        return user;
    }

    // ------------------------------------------------------------------ developers (Hitss)

    public List<DeveloperView> listDevelopers() {
        return store.developers.values().stream()
                .sorted(Comparator.comparing(d -> d.name.toLowerCase()))
                .map(this::developerView)
                .collect(Collectors.toList());
    }

    public synchronized DeveloperView createDeveloper(DeveloperRequest r) {
        if (blank(r.name()) || blank(r.email())) {
            throw new ApiException(400, "Nome e e-mail são obrigatórios.");
        }
        if (!r.email().contains("@")) {
            throw new ApiException(400, "E-mail inválido.");
        }
        boolean exists = store.developers.values().stream()
                .anyMatch(d -> d.email.equalsIgnoreCase(r.email().trim()));
        if (exists) {
            throw new ApiException(409, "Já existe um dev com este e-mail.");
        }
        Developer d = new Developer();
        d.id = store.nextId();
        d.name = r.name().trim();
        d.email = r.email().trim();
        d.jobTitle = text(r.jobTitle(), "cargo");
        d.stack = text(r.stack(), "stack");
        store.developers.put(d.id, d);
        return developerView(d);
    }

    public synchronized DeveloperView updateDeveloper(long id, DeveloperRequest r) {
        Developer d = store.developers.get(id);
        if (d == null) {
            throw new ApiException(404, "Dev não encontrado.");
        }
        if (!blank(r.name())) {
            d.name = r.name().trim();
        }
        if (!blank(r.email())) {
            d.email = r.email().trim();
        }
        if (r.jobTitle() != null) {
            d.jobTitle = text(r.jobTitle(), "cargo");
        }
        if (r.stack() != null) {
            d.stack = text(r.stack(), "stack");
        }
        if (r.active() != null) {
            d.active = r.active();
        }
        return developerView(d);
    }

    public DeveloperDetailView developerDetail(long id) {
        Developer d = store.developers.get(id);
        if (d == null) {
            throw new ApiException(404, "Dev não encontrado.");
        }
        List<Feedback> fs = feedbacksOfDeveloper(id);
        List<AllocationView> allocs = store.allocations.values().stream()
                .filter(a -> a.developerId == id)
                .sorted(Comparator.comparing((Allocation a) -> a.startDate).reversed())
                .map(this::allocationView)
                .collect(Collectors.toList());
        List<FeedbackView> views = fs.stream()
                .sorted(Comparator.comparing((Feedback f) -> f.period).thenComparing(f -> f.id).reversed())
                .map(this::feedbackView)
                .collect(Collectors.toList());
        return new DeveloperDetailView(developerView(d), allocs, views, dimensionAverages(fs), trend(fs, 12));
    }

    private DeveloperView developerView(Developer d) {
        List<Feedback> fs = feedbacksOfDeveloper(d.id);
        LocalDate today = today();
        List<Allocation> active = store.allocations.values().stream()
                .filter(a -> a.developerId == d.id && a.isActive(today))
                .collect(Collectors.toList());
        List<String> managers = active.stream()
                .map(a -> managerOf(a.clientManagerId))
                .filter(Objects::nonNull)
                .map(m -> m.name + " (" + m.organization + ")")
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        String last = fs.stream().map(f -> f.period).max(String::compareTo).orElse(null);
        return new DeveloperView(d.id, d.name, d.email, d.jobTitle, d.stack, d.active, active.size(), fs.size(),
                averageScore(fs), averageRecommend(fs), last, managers);
    }

    private List<Feedback> feedbacksOfDeveloper(long developerId) {
        return store.feedbacks.values().stream()
                .filter(f -> f.developerId == developerId)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ client managers (Hitss)

    public List<ClientManagerView> listClientManagers() {
        LocalDate today = today();
        return store.users.values().stream()
                .filter(u -> u.role == Role.CLIENT_MANAGER)
                .sorted(Comparator.comparing((AppUser u) -> u.organization.toLowerCase())
                        .thenComparing(u -> u.name.toLowerCase()))
                .map(u -> new ClientManagerView(u.id, u.name, u.email, u.organization,
                        (int) store.allocations.values().stream()
                                .filter(a -> a.clientManagerId == u.id && a.isActive(today)).count()))
                .collect(Collectors.toList());
    }

    public synchronized ClientManagerView createClientManager(ClientManagerRequest r) {
        if (blank(r.name()) || blank(r.email()) || blank(r.organization())) {
            throw new ApiException(400, "Nome, e-mail e cliente (organização) são obrigatórios.");
        }
        if (!r.email().contains("@")) {
            throw new ApiException(400, "E-mail inválido.");
        }
        if (blank(r.password()) || r.password().length() < 6) {
            throw new ApiException(400, "Defina uma senha inicial com pelo menos 6 caracteres.");
        }
        boolean exists = store.users.values().stream().anyMatch(u -> u.email.equalsIgnoreCase(r.email().trim()));
        if (exists) {
            throw new ApiException(409, "Já existe um usuário com este e-mail.");
        }
        AppUser u = newUser(r.name().trim(), r.email().trim(), Role.CLIENT_MANAGER, r.organization().trim(),
                r.password());
        return new ClientManagerView(u.id, u.name, u.email, u.organization, 0);
    }

    /** Cria usuário com senha em hash. Tambem usado pelo seed. */
    public AppUser newUser(String name, String email, Role role, String organization, String password) {
        AppUser u = new AppUser();
        u.id = store.nextId();
        u.name = name;
        u.email = email;
        u.role = role;
        u.organization = organization;
        u.salt = hasher.newSalt();
        u.passwordHash = hasher.hash(password, u.salt);
        store.users.put(u.id, u);
        return u;
    }

    // ------------------------------------------------------------------ allocations (Hitss)

    public List<AllocationView> listAllocations(boolean onlyActive) {
        LocalDate today = today();
        return store.allocations.values().stream()
                .filter(a -> !onlyActive || a.isActive(today))
                .sorted(Comparator.comparing((Allocation a) -> a.startDate).reversed().thenComparing(a -> a.id))
                .map(this::allocationView)
                .collect(Collectors.toList());
    }

    public synchronized AllocationView createAllocation(AllocationRequest r) {
        Developer dev = store.developers.get(r.developerId());
        if (dev == null || !dev.active) {
            throw new ApiException(400, "Selecione um dev ativo.");
        }
        AppUser manager = store.users.get(r.clientManagerId());
        if (manager == null || manager.role != Role.CLIENT_MANAGER) {
            throw new ApiException(400, "Selecione um gestor do cliente.");
        }
        if (blank(r.project())) {
            throw new ApiException(400, "Informe o projeto ou squad.");
        }
        LocalDate start;
        try {
            start = blank(r.startDate()) ? today() : LocalDate.parse(r.startDate().trim());
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "Data de início inválida. Use o formato aaaa-mm-dd.");
        }
        LocalDate today = today();
        String project = r.project().trim();
        boolean duplicate = store.allocations.values().stream().anyMatch(a ->
                a.developerId == dev.id && a.clientManagerId == manager.id && a.isActive(today)
                        && a.project.equalsIgnoreCase(project));
        if (duplicate) {
            throw new ApiException(409, "Este dev já está alocado a este gestor neste projeto.");
        }
        Allocation a = new Allocation();
        a.id = store.nextId();
        a.developerId = dev.id;
        a.clientManagerId = manager.id;
        a.project = project;
        a.startDate = start;
        store.allocations.put(a.id, a);
        return allocationView(a);
    }

    public synchronized AllocationView endAllocation(long id) {
        Allocation a = store.allocations.get(id);
        if (a == null) {
            throw new ApiException(404, "Alocação não encontrada.");
        }
        if (a.isActive(today())) {
            a.endDate = today();
        }
        return allocationView(a);
    }

    private AllocationView allocationView(Allocation a) {
        Developer d = store.developers.get(a.developerId);
        AppUser m = managerOf(a.clientManagerId);
        return new AllocationView(a.id, a.developerId, d == null ? "?" : d.name, a.clientManagerId,
                m == null ? "?" : m.name, m == null ? "?" : m.organization, a.project,
                a.startDate.toString(), a.endDate == null ? null : a.endDate.toString(), a.isActive(today()));
    }

    // ------------------------------------------------------------------ feedbacks

    /** Listagem para a Hitss, com filtros opcionais. Mais recentes primeiro. */
    public List<FeedbackView> listFeedbacks(Long developerId, String period, Long clientManagerId) {
        return store.feedbacks.values().stream()
                .filter(f -> developerId == null || f.developerId == developerId)
                .filter(f -> blank(period) || f.period.equals(period))
                .filter(f -> clientManagerId == null || f.clientManagerId == clientManagerId)
                .sorted(Comparator.comparing((Feedback f) -> f.period).thenComparing(f -> f.id).reversed())
                .map(this::feedbackView)
                .collect(Collectors.toList());
    }

    /** Feedbacks dados pelo próprio gestor do cliente. */
    public List<FeedbackView> myFeedbacks(AppUser manager, Long developerId) {
        return store.feedbacks.values().stream()
                .filter(f -> f.clientManagerId == manager.id)
                .filter(f -> developerId == null || f.developerId == developerId)
                .sorted(Comparator.comparing((Feedback f) -> f.period).thenComparing(f -> f.id).reversed())
                .map(this::feedbackView)
                .collect(Collectors.toList());
    }

    public FeedbackView myFeedback(AppUser manager, long id) {
        Feedback f = store.feedbacks.get(id);
        if (f == null || f.clientManagerId != manager.id) {
            throw new ApiException(404, "Feedback não encontrado.");
        }
        return feedbackView(f);
    }

    public synchronized FeedbackView createFeedback(AppUser manager, FeedbackRequest r) {
        Allocation a = store.allocations.get(r.allocationId());
        if (a == null || a.clientManagerId != manager.id) {
            throw new ApiException(404, "Alocação não encontrada para este gestor.");
        }
        if (!a.isActive(today())) {
            throw new ApiException(409, "Esta alocação foi encerrada.");
        }
        String period = currentPeriod();
        boolean exists = store.feedbacks.values().stream()
                .anyMatch(f -> f.allocationId == a.id && f.period.equals(period));
        if (exists) {
            throw new ApiException(409,
                    "Já existe feedback de " + period + " para este profissional. Edite o feedback existente.");
        }
        validate(r);
        Feedback f = new Feedback();
        f.id = store.nextId();
        f.allocationId = a.id;
        f.developerId = a.developerId;
        f.clientManagerId = manager.id;
        f.period = period;
        f.createdAt = Instant.now();
        fill(f, r);
        store.feedbacks.put(f.id, f);
        return feedbackView(f);
    }

    public synchronized FeedbackView updateFeedback(AppUser manager, long id, FeedbackRequest r) {
        Feedback f = store.feedbacks.get(id);
        if (f == null || f.clientManagerId != manager.id) {
            throw new ApiException(404, "Feedback não encontrado.");
        }
        if (!f.period.equals(currentPeriod())) {
            throw new ApiException(409, "Feedbacks de competências anteriores não podem ser alterados.");
        }
        validate(r);
        fill(f, r);
        return feedbackView(f);
    }

    private void validate(FeedbackRequest r) {
        range(r.technical(), 1, 5, "Qualidade técnica");
        range(r.communication(), 1, 5, "Comunicação");
        range(r.proactivity(), 1, 5, "Proatividade");
        range(r.commitment(), 1, 5, "Compromisso com prazos");
        range(r.teamwork(), 1, 5, "Trabalho em equipe");
        range(r.recommend(), 0, 10, "Recomendação");
    }

    private static void range(Integer v, int min, int max, String label) {
        if (v == null || v < min || v > max) {
            throw new ApiException(400, label + ": informe uma nota de " + min + " a " + max + ".");
        }
    }

    private void fill(Feedback f, FeedbackRequest r) {
        f.technical = r.technical();
        f.communication = r.communication();
        f.proactivity = r.proactivity();
        f.commitment = r.commitment();
        f.teamwork = r.teamwork();
        f.recommend = r.recommend();
        f.strengths = text(r.strengths(), "pontos fortes");
        f.improvements = text(r.improvements(), "pontos de melhoria");
        f.comment = text(r.comment(), "comentário");
        f.updatedAt = Instant.now();
    }

    public FeedbackView feedbackView(Feedback f) {
        Developer d = store.developers.get(f.developerId);
        AppUser m = managerOf(f.clientManagerId);
        Allocation a = store.allocations.get(f.allocationId);
        return new FeedbackView(f.id, f.allocationId, f.developerId, d == null ? "?" : d.name, f.clientManagerId,
                m == null ? "?" : m.name, m == null ? "?" : m.organization, a == null ? "?" : a.project, f.period,
                f.technical, f.communication, f.proactivity, f.commitment, f.teamwork, f.recommend,
                round2(f.score()), f.strengths, f.improvements, f.comment,
                f.createdAt == null ? null : f.createdAt.toString(),
                f.updatedAt == null ? null : f.updatedAt.toString());
    }

    // ------------------------------------------------------------------ client team

    public TeamView team(AppUser manager) {
        LocalDate today = today();
        String period = currentPeriod();
        List<TeamMemberView> members = new ArrayList<>();
        List<Allocation> mine = store.allocations.values().stream()
                .filter(a -> a.clientManagerId == manager.id && a.isActive(today))
                .sorted(Comparator.comparing(a -> {
                    Developer d = store.developers.get(a.developerId);
                    return d == null ? "" : d.name.toLowerCase();
                }))
                .collect(Collectors.toList());
        int done = 0;
        for (Allocation a : mine) {
            Developer d = store.developers.get(a.developerId);
            if (d == null) {
                continue;
            }
            List<Feedback> history = store.feedbacks.values().stream()
                    .filter(f -> f.allocationId == a.id)
                    .sorted(Comparator.comparing((Feedback f) -> f.period).reversed())
                    .collect(Collectors.toList());
            Feedback thisPeriod = history.stream().filter(f -> f.period.equals(period)).findFirst().orElse(null);
            Feedback latest = history.isEmpty() ? null : history.get(0);
            if (thisPeriod != null) {
                done++;
            }
            members.add(new TeamMemberView(a.id, d.id, d.name, d.jobTitle, d.stack, a.project,
                    a.startDate.toString(), thisPeriod != null, thisPeriod == null ? null : thisPeriod.id,
                    latest == null ? null : round2(latest.score()), latest == null ? null : latest.period));
        }
        return new TeamView(period, members.size(), done, members);
    }

    // ------------------------------------------------------------------ dashboard (Hitss)

    public DashboardView dashboard() {
        LocalDate today = today();
        String period = currentPeriod();
        List<Allocation> active = store.allocations.values().stream()
                .filter(a -> a.isActive(today))
                .collect(Collectors.toList());
        List<Feedback> all = new ArrayList<>(store.feedbacks.values());
        List<Feedback> thisPeriod = all.stream().filter(f -> f.period.equals(period)).collect(Collectors.toList());

        long activeDevs = active.stream().map(a -> a.developerId).distinct().count();
        long activeManagers = active.stream().map(a -> a.clientManagerId).distinct().count();
        int done = (int) active.stream().filter(a -> thisPeriod.stream().anyMatch(f -> f.allocationId == a.id))
                .count();
        int pending = active.size() - done;
        int coverage = active.isEmpty() ? 0 : (int) Math.round(done * 100.0 / active.size());

        List<DevSummary> attention = new ArrayList<>();
        List<DevSummary> highlights = new ArrayList<>();
        for (Developer d : store.developers.values()) {
            if (!d.active) {
                continue;
            }
            TreeMap<String, Double> byPeriod = periodAverages(feedbacksOfDeveloper(d.id));
            if (byPeriod.isEmpty()) {
                continue;
            }
            String latestPeriod = byPeriod.lastKey();
            double latest = byPeriod.get(latestPeriod);
            String previousPeriod = byPeriod.lowerKey(latestPeriod);
            Double previous = previousPeriod == null ? null : byPeriod.get(previousPeriod);
            if (latest < 3.5) {
                attention.add(new DevSummary(d.id, d.name, round2(latest),
                        "Nota média abaixo de 3,5 em " + latestPeriod));
            } else if (previous != null && latest <= previous - 0.5) {
                attention.add(new DevSummary(d.id, d.name, round2(latest),
                        "Queda de " + String.format(java.util.Locale.ROOT, "%.1f", previous - latest).replace('.', ',')
                                + " ponto(s) de " + previousPeriod + " para " + latestPeriod));
            } else if (latest >= 4.5) {
                highlights.add(new DevSummary(d.id, d.name, round2(latest),
                        "Nota média " + String.format(java.util.Locale.ROOT, "%.1f", latest).replace('.', ',') + " em "
                                + latestPeriod));
            }
        }
        attention.sort(Comparator.comparing((DevSummary s) -> s.averageScore()));
        highlights.sort(Comparator.comparing((DevSummary s) -> s.averageScore()).reversed());

        List<FeedbackView> recent = all.stream()
                .sorted(Comparator.comparing((Feedback f) -> f.createdAt).reversed())
                .limit(6)
                .map(this::feedbackView)
                .collect(Collectors.toList());

        return new DashboardView(period, (int) activeDevs, (int) activeManagers, active.size(), thisPeriod.size(),
                pending, coverage, averageScore(all), nps(all), dimensionAverages(all), trend(all, 6),
                attention, highlights, recent);
    }

    // ------------------------------------------------------------------ estatísticas

    private Double averageScore(List<Feedback> fs) {
        if (fs.isEmpty()) {
            return null;
        }
        return round2(fs.stream().mapToDouble(Feedback::score).average().orElse(0));
    }

    private Double averageRecommend(List<Feedback> fs) {
        if (fs.isEmpty()) {
            return null;
        }
        return round2(fs.stream().mapToInt(f -> f.recommend).average().orElse(0));
    }

    /** NPS = % promotores (9-10) menos % detratores (0-6), arredondado. Null sem dados. */
    private Integer nps(List<Feedback> fs) {
        if (fs.isEmpty()) {
            return null;
        }
        long promoters = fs.stream().filter(f -> f.recommend >= 9).count();
        long detractors = fs.stream().filter(f -> f.recommend <= 6).count();
        return (int) Math.round((promoters - detractors) * 100.0 / fs.size());
    }

    private Map<String, Double> dimensionAverages(List<Feedback> fs) {
        Map<String, Double> m = new LinkedHashMap<>();
        if (fs.isEmpty()) {
            return m;
        }
        m.put("technical", round2(fs.stream().mapToInt(f -> f.technical).average().orElse(0)));
        m.put("communication", round2(fs.stream().mapToInt(f -> f.communication).average().orElse(0)));
        m.put("proactivity", round2(fs.stream().mapToInt(f -> f.proactivity).average().orElse(0)));
        m.put("commitment", round2(fs.stream().mapToInt(f -> f.commitment).average().orElse(0)));
        m.put("teamwork", round2(fs.stream().mapToInt(f -> f.teamwork).average().orElse(0)));
        return m;
    }

    private TreeMap<String, Double> periodAverages(List<Feedback> fs) {
        Map<String, List<Feedback>> grouped = fs.stream().collect(Collectors.groupingBy(f -> f.period));
        TreeMap<String, Double> out = new TreeMap<>();
        grouped.forEach((p, list) -> out.put(p, list.stream().mapToDouble(Feedback::score).average().orElse(0)));
        return out;
    }

    /** Serie mensal (média de nota e quantidade), limitada as últimas N competências com dados. */
    private List<TrendPoint> trend(List<Feedback> fs, int lastN) {
        Map<String, List<Feedback>> grouped = fs.stream().collect(Collectors.groupingBy(f -> f.period));
        List<String> periods = new ArrayList<>(new TreeMap<>(grouped).keySet());
        int from = Math.max(0, periods.size() - lastN);
        List<TrendPoint> out = new ArrayList<>();
        for (String p : periods.subList(from, periods.size())) {
            List<Feedback> list = grouped.get(p);
            out.add(new TrendPoint(p, averageScore(list), list.size()));
        }
        return out;
    }
}
