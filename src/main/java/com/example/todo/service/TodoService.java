package com.example.todo.service;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.TodoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {
  private final TodoRepository todoRepository;

  @Autowired
  public TodoService(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  public List<Todo> getTodos(
            String filterByStatus, // "ALL", "COMPLETED", "PENDING"
            Priority filterByPriority,
            String filterByDueDate, // "ALL", "OVERDUE", "TODAY", "NEXT_7_DAYS", or YYYY-MM-DD
            String searchTerm,
            String sortBy, // "creationDate", "dueDate", "priority", "title"
            String sortDir  // "ASC", "DESC"
  ) {
    Specification<Todo> spec = Specification.where(TodoSpecification.isTopLevelTask());

    // Status Filter
    if ("COMPLETED".equalsIgnoreCase(filterByStatus)) {
        spec = spec.and(TodoSpecification.hasStatus(true));
    } else if ("PENDING".equalsIgnoreCase(filterByStatus)) {
        spec = spec.and(TodoSpecification.hasStatus(false));
    }

    // Priority Filter
    if (filterByPriority != null) {
        spec = spec.and(TodoSpecification.hasPriority(filterByPriority));
    }

    // Due Date Filter
    LocalDate today = LocalDate.now();
    if (filterByDueDate != null && !filterByDueDate.trim().isEmpty() && !"ALL".equalsIgnoreCase(filterByDueDate)) {
        try {
            // Try to parse as a specific date first
            LocalDate specificDueDate = LocalDate.parse(filterByDueDate); // Expects YYYY-MM-DD
            spec = spec.and(TodoSpecification.hasDueDate(specificDueDate));
        } catch (DateTimeParseException e) {
            // If not a specific date, check for keywords
            if ("OVERDUE".equalsIgnoreCase(filterByDueDate)) {
                spec = spec.and(TodoSpecification.isOverdue(today));
            } else if ("TODAY".equalsIgnoreCase(filterByDueDate)) {
                spec = spec.and(TodoSpecification.dueToday(today));
            } else if ("NEXT_7_DAYS".equalsIgnoreCase(filterByDueDate)) {
                spec = spec.and(TodoSpecification.dueNextDays(today, 7));
            }
            // else: unknown keyword, ignore or log warning
        }
    }

    // Search Term Filter (titleContains already returns alwaysTrue if searchTerm is blank)
    spec = spec.and(TodoSpecification.titleContains(searchTerm));

    // Sorting
    Sort.Direction direction = "DESC".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    String sortField = "creationDate"; // Default sort field
    if ("dueDate".equalsIgnoreCase(sortBy)) sortField = "dueDate";
    else if ("priority".equalsIgnoreCase(sortBy)) sortField = "priority";
    else if ("title".equalsIgnoreCase(sortBy)) sortField = "title";
    // "creationDate" is already the default if sortBy is null or unrecognized

    Sort sort = Sort.by(direction, sortField);

    return todoRepository.findAll(spec, sort);
  }

  public Todo addTodo(String title, LocalDate dueDate, Priority priority) {
    Todo todo = new Todo(); // Default priority (MEDIUM) is set by the constructor
    todo.setTitle(title);
    todo.setCompleted(false);
    todo.setDueDate(dueDate);
    if (priority != null) { // Override default if a specific priority is provided
        todo.setPriority(priority);
    }
    return todoRepository.save(todo);
  }

  public void deleteTodo(long id) {
    todoRepository.deleteById(id);
  }

  public void markCompleted(long id) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    optionalTodo.ifPresent(todo -> {
      todo.setCompleted(true);
      todoRepository.save(todo);
    });
  }

  public Optional<Todo> updateTodo(long id, String title, LocalDate dueDate, Priority priority) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    return optionalTodo.map(todo -> {
      todo.setTitle(title);
      todo.setDueDate(dueDate);
      if (priority != null) { // Update priority only if a new one is provided
        todo.setPriority(priority);
      }
      return todoRepository.save(todo);
    });
  }

  public Optional<Todo> findTodoById(long id) {
    return todoRepository.findById(id);
  }

  public Optional<Todo> addSubTask(Long parentId, String title, LocalDate dueDate, Priority priority) {
    Optional<Todo> parentTodoOptional = todoRepository.findById(parentId);
    if (parentTodoOptional.isPresent()) {
      Todo parentTodo = parentTodoOptional.get();
      Todo subTask = new Todo(); // Default priority (MEDIUM) is set by constructor
      subTask.setTitle(title);
      subTask.setCompleted(false);
      subTask.setDueDate(dueDate);
      if (priority != null) { // Override default if a specific priority is provided
        subTask.setPriority(priority);
      }
      subTask.setParent(parentTodo);
      // parentTodo.addSubTask(subTask); // Optional: for in-memory consistency if parentTodo is further used
      return Optional.of(todoRepository.save(subTask));
    } else {
      return Optional.empty(); // Or throw new EntityNotFoundException("Parent task with id " + parentId + " not found");
    }
  }
}
