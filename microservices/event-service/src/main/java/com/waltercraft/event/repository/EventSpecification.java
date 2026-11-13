package com.waltercraft.event.repository;

import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> filter(
            List<Integer> seasons,
            List<String> organizers,
            List<EventStatus> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Boolean activeOnly
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Boolean.TRUE.equals(activeOnly)) {
                predicates.add(cb.isTrue(root.get("active")));
            }

            if (seasons != null && !seasons.isEmpty()) {
                predicates.add(root.get("seasonNumber").in(seasons));
            }

            if (organizers != null && !organizers.isEmpty()) {
                List<String> normalized = organizers.stream()
                        .filter(o -> o != null && !o.isBlank())
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();
                if (!normalized.isEmpty()) {
                    predicates.add(cb.lower(root.get("organizer")).in(normalized));
                }
            }

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            if (dateFrom != null) {
                // Event is on or after dateFrom: endDate >= dateFrom OR (endDate is null AND startDate >= dateFrom)
                Predicate endAfter = cb.greaterThanOrEqualTo(root.get("endDate"), dateFrom);
                Predicate startAfter = cb.greaterThanOrEqualTo(root.get("startDate"), dateFrom);
                predicates.add(cb.or(endAfter, startAfter));
            }

            if (dateTo != null) {
                // Event starts on or before dateTo
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), dateTo));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate shortDescLike = cb.like(cb.lower(root.get("shortDescription")), pattern);
                predicates.add(cb.or(titleLike, descLike, shortDescLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
