package com.example.todo.repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodoSpecification {

    public static Specification<Todo> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static Specification<Todo> isTopLevelTask() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent"));
    }

    public static Specification<Todo> hasStatus(Boolean completed) {
        if (completed == null) {
            return alwaysTrue(); // Or (root, query, cb) -> cb.conjunction(); to not add any restriction
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("completed"), completed);
    }

    public static Specification<Todo> hasPriority(Priority priority) {
        if (priority == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Todo> hasDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dueDate"), dueDate);
    }

    public static Specification<Todo> isOverdue(LocalDate today) {
        if (today == null) return alwaysTrue();
         return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("dueDate"), today),
                criteriaBuilder.isNotNull(root.get("dueDate")), // Ensure dueDate is not null
                criteriaBuilder.equal(root.get("completed"), false)
            );
    }

    public static Specification<Todo> dueToday(LocalDate today) {
        if (today == null) return alwaysTrue();
         return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dueDate"), today);
    }

    public static Specification<Todo> dueNextDays(LocalDate startDate, int days) {
        if (startDate == null) return alwaysTrue();
        LocalDate endDate = startDate.plusDays(days -1); // Inclusive of startDate and endDate
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("dueDate")),
                criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDate),
                criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDate)
            );
    }

    public static Specification<Todo> titleContains(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchTerm.toLowerCase() + "%");
    }

    // Helper to combine specifications, typically used in a service
    public static Specification<Todo> combine(Specification<Todo> base, Specification<Todo> toAdd) {
        if (toAdd == null) {
            return base;
        }
        if (base == null) {
            return toAdd;
        }
        return base.and(toAdd);
    }
}
