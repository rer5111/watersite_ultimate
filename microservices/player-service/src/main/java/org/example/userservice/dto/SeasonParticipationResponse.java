package org.example.userservice.dto;

import org.example.userservice.model.SeasonModel;
import org.example.userservice.model.UserModel;

import java.time.LocalDate;
import java.util.List;

public record SeasonParticipationResponse(long playerId, int totalSeasons, List<SeasonEntry> seasons) {

    public SeasonParticipationResponse {
        seasons = List.copyOf(seasons);
    }

    public static SeasonParticipationResponse from(UserModel player) {
        List<SeasonEntry> entries = player.getSeasons().stream()
                .sorted((left, right) -> {
                    if (left.getStartDate() == null) return right.getStartDate() == null ? 0 : 1;
                    if (right.getStartDate() == null) return -1;
                    return left.getStartDate().compareTo(right.getStartDate());
                })
                .map(SeasonEntry::from)
                .toList();
        return new SeasonParticipationResponse(player.getId(), entries.size(), entries);
    }

    public static SeasonParticipationResponse empty(long playerId) {
        return new SeasonParticipationResponse(playerId, 0, List.of());
    }

    public record SeasonEntry(
            long id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            String image,
            Phase phase
    ) {
        private static SeasonEntry from(SeasonModel season) {
            return new SeasonEntry(
                    season.getId(),
                    season.getTitle(),
                    season.getStartDate(),
                    season.getEndDate(),
                    season.getDescription(),
                    season.getImage(),
                    Phase.from(season.getStartDate(), season.getEndDate()));
        }
    }

    public enum Phase {
        UPCOMING,
        ACTIVE,
        COMPLETED,
        UNKNOWN;

        private static Phase from(LocalDate startDate, LocalDate endDate) {
            if (startDate == null || endDate == null) return UNKNOWN;
            LocalDate today = LocalDate.now();
            if (today.isBefore(startDate)) return UPCOMING;
            if (today.isAfter(endDate)) return COMPLETED;
            return ACTIVE;
        }
    }
}
