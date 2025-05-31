package com.example.todo.service;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.TodoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Default to read-only for GET methods
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
    Direction direction = "DESC".equalsIgnoreCase(sortDir) ? Direction.DESC : Direction.ASC;
    String sortField = "displayOrder"; // New default sort field

    if (sortBy != null && !sortBy.trim().isEmpty() && !"manual".equalsIgnoreCase(sortBy) && !"displayOrder".equalsIgnoreCase(sortBy)) {
        if ("dueDate".equalsIgnoreCase(sortBy)) sortField = "dueDate";
        else if ("priority".equalsIgnoreCase(sortBy)) sortField = "priority";
        else if ("title".equalsIgnoreCase(sortBy)) sortField = "title";
        else if ("creationDate".equalsIgnoreCase(sortBy)) sortField = "creationDate";
        // If sortBy is something else unrecognized, it defaults to displayOrder
    }

    Sort sort;
    if (!"displayOrder".equals(sortField)) {
        // Add displayOrder as a secondary sort for stability if sorting by something else
        sort = Sort.by(direction, sortField).and(Sort.by(Direction.ASC, "displayOrder"));
    } else {
        sort = Sort.by(direction, sortField); // Primary sort by displayOrder
    }

    return todoRepository.findAll(spec, sort);
  }

  @Transactional // Make it transactional as it modifies data
  public Todo addTodo(String title, LocalDate dueDate, Priority priority) {
    Todo todo = new Todo(); // Default priority (MEDIUM) is set by the constructor
    todo.setTitle(title);
    todo.setCompleted(false);
    todo.setDueDate(dueDate);
    if (priority != null) { // Override default if a specific priority is provided
        todo.setPriority(priority);
    }

    Integer maxOrder = todoRepository.findMaxDisplayOrderByParentIsNull();
    todo.setDisplayOrder(maxOrder == null ? 0 : maxOrder + 1);

    return todoRepository.save(todo);
  }

  @Transactional
  public void deleteTodo(long id) {
    todoRepository.deleteById(id);
  }

  @Transactional
  public void markCompleted(long id) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    optionalTodo.ifPresent(todo -> {
      todo.setCompleted(true);
      todoRepository.save(todo);
    });
  }

  @Transactional
  public Optional<Todo> updateTodo(long id, String title, LocalDate dueDate, Priority priority) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    return optionalTodo.map(todo -> {
      todo.setTitle(title);
      todo.setDueDate(dueDate);
      if (priority != null) { // Update priority only if a new one is provided
        todo.setPriority(priority);
      }
      // Note: displayOrder is not updated here, it's handled by updateTaskOrder
      return todoRepository.save(todo);
    });
  }

  public Optional<Todo> findTodoById(long id) {
    return todoRepository.findById(id);
  }

  @Transactional
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

      Integer maxOrder = todoRepository.findMaxDisplayOrderByParent(parentTodo);
      subTask.setDisplayOrder(maxOrder == null ? 0 : maxOrder + 1);

      // parentTodo.addSubTask(subTask); // Optional: for in-memory consistency if parentTodo is further used
      return Optional.of(todoRepository.save(subTask));
    } else {
      return Optional.empty(); // Or throw new EntityNotFoundException("Parent task with id " + parentId + " not found");
    }
  }

  @Transactional
  public void updateTaskOrder(List<Long> orderedTaskIds, Long nullableParentId) {
      List<Todo> tasksToUpdate = new ArrayList<>();
      for (int i = 0; i < orderedTaskIds.size(); i++) {
          Long taskId = orderedTaskIds.get(i);
          int newOrder = i; // The new displayOrder is the index in the list
          Optional<Todo> optionalTodo = todoRepository.findById(taskId);
          if (optionalTodo.isPresent()) {
              Todo task = optionalTodo.get();

              boolean parentMatch = (nullableParentId == null && task.getParent() == null) ||
                                    (nullableParentId != null && task.getParent() != null && nullableParentId.equals(task.getParent().getId()));

              if (parentMatch) {
                  if (task.getDisplayOrder() == null || task.getDisplayOrder() != newOrder) {
                    task.setDisplayOrder(newOrder);
                    tasksToUpdate.add(task);
                  }
              } else {
                  // Log error or throw exception if a task does not belong to the expected parent context
                  // This indicates a potential issue with the data sent from the client
                  System.err.println("Task " + taskId + " (current parent: " + (task.getParent() != null ? task.getParent().getId() : "null")
                                     + ") does not match expected parent context: " + nullableParentId + ". Skipping order update for this task.");
              }
          } else {
             System.err.println("Task with ID " + taskId + " not found during order update. Skipping.");
          }
      }
      if (!tasksToUpdate.isEmpty()) {
        todoRepository.saveAll(tasksToUpdate);
      }
  }
}
