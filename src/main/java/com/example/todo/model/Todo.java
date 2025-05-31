package com.example.todo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
// Import Priority Enum (already in the same package, so not strictly necessary for compilation,
// but good for clarity and if it were in a different package)
// import com.example.todo.model.Priority;

@Entity
public class Todo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String title;
  private boolean completed;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  private Priority priority;

  public Todo() {
    this.priority = Priority.MEDIUM; // Default priority
  }

  public Todo(long id, String title, boolean completed, LocalDate dueDate, Priority priority) {
    this.id = id;
    this.title = title;
    this.completed = completed;
    this.dueDate = dueDate;
    this.priority = (priority != null) ? priority : Priority.MEDIUM; // Ensure non-null, default if necessary
  }

  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public boolean isCompleted() {
    return completed;
  }
  
  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public boolean isOverdue() {
    if (this.dueDate == null || this.isCompleted()) {
      return false;
    }
    return java.time.LocalDate.now().isAfter(this.dueDate);
  }

  public boolean isDueSoon(int days) {
    if (this.dueDate == null || this.isCompleted()) {
      return false;
    }
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDate soonDate = today.plusDays(days);
    // Due date is between today (inclusive) and 'days' from now (inclusive of soonDate)
    // and not in the past (unless today is the due date)
    return !this.dueDate.isBefore(today) && !this.dueDate.isAfter(soonDate);
  }
}
