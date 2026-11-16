package br.com.hitss.elo.api;

import br.com.hitss.elo.domain.Role;

import java.util.List;
import java.util.Map;

/** Contratos JSON da API (requests e views). Datas sempre como String ISO (yyyy-MM-dd). */
public final class Dto {
    private Dto() {
    }

    // ---- Auth
    public record LoginRequest(String email, String password) {
    }

    public record UserView(long id, String name, String email, Role role, String organization) {
    }

    public record LoginResponse(String token, UserView user) {
    }

    // ---- Developers
    public record DeveloperRequest(String name, String email, String jobTitle, String stack, Boolean active) {
    }

    public record DeveloperView(long id, String name, String email, String jobTitle, String stack, boolean active,
                                int activeAllocations, int feedbackCount, Double averageScore,
                                Double recommendAverage, String lastFeedbackPeriod, List<String> clientManagers) {
    }

    public record DeveloperDetailView(DeveloperView developer, List<AllocationView> allocations,
                                      List<FeedbackView> feedbacks, Map<String, Double> dimensionAverages,
                                      List<TrendPoint> trend) {
    }

    // ---- Client managers
    public record ClientManagerRequest(String name, String email, String organization, String password) {
    }

    public record ClientManagerView(long id, String name, String email, String organization, int activeAllocations) {
    }

    // ---- Allocations
    public record AllocationRequest(long developerId, long clientManagerId, String project, String startDate) {
    }

    public record AllocationView(long id, long developerId, String developerName, long clientManagerId,
                                 String clientManagerName, String clientOrganization, String project,
                                 String startDate, String endDate, boolean active) {
    }

    // ---- Feedbacks
    public record FeedbackRequest(long allocationId, Integer technical, Integer communication, Integer proactivity,
                                  Integer commitment, Integer teamwork, Integer recommend, String strengths,
                                  String improvements, String comment) {
    }

    public record FeedbackView(long id, long allocationId, long developerId, String developerName,
                               long clientManagerId, String clientManagerName, String clientOrganization,
                               String project, String period, int technical, int communication, int proactivity,
                               int commitment, int teamwork, int recommend, double score, String strengths,
                               String improvements, String comment, String createdAt, String updatedAt) {
    }

    // ---- Client area
    public record TeamMemberView(long allocationId, long developerId, String developerName, String jobTitle,
                                 String stack, String project, String startDate, boolean feedbackDone,
                                 Long feedbackId, Double lastScore, String lastPeriod) {
    }

    public record TeamView(String period, int total, int done, List<TeamMemberView> members) {
    }

    // ---- Dashboard Hitss
    public record TrendPoint(String period, Double averageScore, int count) {
    }

    public record DevSummary(long developerId, String developerName, Double averageScore, String note) {
    }

    public record DashboardView(String period, int activeDevelopers, int activeClientManagers,
                                int activeAllocations, int feedbacksThisPeriod, int pendingThisPeriod,
                                int coveragePercent, Double averageScore, Integer nps,
                                Map<String, Double> dimensionAverages, List<TrendPoint> trend,
                                List<DevSummary> attention, List<DevSummary> highlights,
                                List<FeedbackView> recentFeedbacks) {
    }

    // ---- Erros
    public record ErrorBody(String message) {
    }
}
