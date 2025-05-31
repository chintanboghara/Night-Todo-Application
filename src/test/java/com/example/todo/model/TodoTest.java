package com.example.todo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class TodoTest {

    private Todo todo;

    @BeforeEach
    void setUp() {
        todo = new Todo();
        todo.setTitle("Test Task");
        todo.setCompleted(false);
    }

    // Tests for isOverdue()
    @Test
    void isOverdue_whenDueDateInPastAndNotCompleted_shouldReturnTrue() {
        todo.setDueDate(LocalDate.now().minusDays(1));
        todo.setCompleted(false);
        assertTrue(todo.isOverdue());
    }

    @Test
    void isOverdue_whenDueDateInFutureAndNotCompleted_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now().plusDays(1));
        todo.setCompleted(false);
        assertFalse(todo.isOverdue());
    }

    @Test
    void isOverdue_whenDueDateIsTodayAndNotCompleted_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now());
        todo.setCompleted(false);
        assertFalse(todo.isOverdue());
    }

    @Test
    void isOverdue_whenDueDateInPastButCompleted_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now().minusDays(1));
        todo.setCompleted(true);
        assertFalse(todo.isOverdue());
    }

    @Test
    void isOverdue_whenNoDueDate_shouldReturnFalse() {
        todo.setDueDate(null);
        todo.setCompleted(false);
        assertFalse(todo.isOverdue());
    }

    // Tests for isDueSoon(int days)
    @Test
    void isDueSoon_whenDueDateIsToday_shouldReturnTrue() {
        todo.setDueDate(LocalDate.now());
        todo.setCompleted(false);
        assertTrue(todo.isDueSoon(7)); // Check within next 7 days
    }

    @Test
    void isDueSoon_whenDueDateIsWithinRange_shouldReturnTrue() {
        todo.setDueDate(LocalDate.now().plusDays(3));
        todo.setCompleted(false);
        assertTrue(todo.isDueSoon(7));
    }

    @Test
    void isDueSoon_whenDueDateIsExactlyAtEndOfRange_shouldReturnTrue() {
        todo.setDueDate(LocalDate.now().plusDays(7));
        todo.setCompleted(false);
        assertTrue(todo.isDueSoon(7));
    }

    @Test
    void isDueSoon_whenDueDateIsOutsideRange_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now().plusDays(8));
        todo.setCompleted(false);
        assertFalse(todo.isDueSoon(7));
    }

    @Test
    void isDueSoon_whenDueDateIsInPast_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now().minusDays(1));
        todo.setCompleted(false);
        assertFalse(todo.isDueSoon(7));
    }

    @Test
    void isDueSoon_whenTaskIsCompleted_shouldReturnFalse() {
        todo.setDueDate(LocalDate.now().plusDays(3));
        todo.setCompleted(true);
        assertFalse(todo.isDueSoon(7));
    }

    @Test
    void isDueSoon_whenNoDueDate_shouldReturnFalse() {
        todo.setDueDate(null);
        todo.setCompleted(false);
        assertFalse(todo.isDueSoon(7));
    }
}
