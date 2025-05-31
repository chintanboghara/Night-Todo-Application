package com.example.todo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.PrePersist;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
// Priority Enum is in the same package

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

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Todo parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Todo> subTasks = new ArrayList<>();

  private LocalDateTime creationDate;

  @PrePersist
  protected void onCreate() {
    if (creationDate == null) {
      creationDate = LocalDateTime.now();
    }
  }

  public Todo() {
    this.priority = Priority.MEDIUM; // Default priority
    // subTasks is initialized at field declaration
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

  public Todo getParent() {
    return parent;
  }

  public void setParent(Todo parent) {
    this.parent = parent;
  }

  public List<Todo> getSubTasks() {
    return subTasks;
  }

  public void setSubTasks(List<Todo> subTasks) {
    this.subTasks = subTasks;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  // Utility methods for managing subtasks
  public void addSubTask(Todo subTask) {
    this.subTasks.add(subTask);
    subTask.setParent(this);
  }

  public void removeSubTask(Todo subTask) {
    this.subTasks.remove(subTask);
    subTask.setParent(null);
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
