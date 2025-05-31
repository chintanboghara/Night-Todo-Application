package com.example.todo.controller;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List; // Added for ReorderRequestPayload
import java.util.Optional;

@Controller
public class TodoController {

    // Static inner class for the reorder request payload
    public static class ReorderRequestPayload {
        private List<Long> orderedTaskIds;
        private Long parentId; // Can be null for top-level tasks

        public List<Long> getOrderedTaskIds() {
            return orderedTaskIds;
        }

        public void setOrderedTaskIds(List<Long> orderedTaskIds) {
            this.orderedTaskIds = orderedTaskIds;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
    }

  @Autowired
  private TodoService todoService;

  @GetMapping("/")
  public String index(
          @RequestParam(required = false, defaultValue = "ALL") String filterByStatus,
          @RequestParam(required = false) Priority filterByPriority,
          @RequestParam(required = false, defaultValue = "ALL") String filterByDueDate,
          @RequestParam(required = false) String searchTerm,
          @RequestParam(required = false, defaultValue = "creationDate") String sortBy,
          @RequestParam(required = false, defaultValue = "ASC") String sortDir,
          Model model
  ) {
    model.addAttribute("todos", todoService.getTodos(filterByStatus, filterByPriority, filterByDueDate, searchTerm, sortBy, sortDir));

    // Add current filter/sort parameters to model for UI to reflect current state
    model.addAttribute("currentFilterByStatus", filterByStatus);
    model.addAttribute("currentFilterByPriority", filterByPriority);
    model.addAttribute("currentFilterByDueDate", filterByDueDate);
    model.addAttribute("currentSearchTerm", searchTerm);
    model.addAttribute("currentSortBy", sortBy);
    model.addAttribute("currentSortDir", sortDir);

    // Add data for dropdowns
    model.addAttribute("priorities", Priority.values()); // For priority filter dropdown

    return "index";
  }

  @PostMapping("/add")
  public String addTodo(@RequestParam("title") String title,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                        @RequestParam(required = false) Priority priority) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.addTodo(title, dueDate, priority);
    }
    return "redirect:/";
  }

  @PostMapping("/delete")
  public String deleteTodo(@RequestParam("id") long id) {
    todoService.deleteTodo(id);
    return "redirect:/";
  }

  @PostMapping("/complete")
  public String completeTodo(@RequestParam("id") long id) {
    todoService.markCompleted(id);
    return "redirect:/";
  }

  @GetMapping("/edit/{id}")
  public String editTodoForm(@PathVariable long id, Model model) {
    Optional<Todo> optionalTodo = todoService.findTodoById(id);
    if (optionalTodo.isPresent()) {
      model.addAttribute("todo", optionalTodo.get());
      model.addAttribute("priorities", Priority.values()); // Add all priority values
      return "edit-todo";
    } else {
      return "redirect:/";
    }
  }

  @PostMapping("/update/{id}")
  public String updateTodo(@PathVariable long id, @RequestParam String title,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                         @RequestParam(required = false) Priority priority) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.updateTodo(id, title, dueDate, priority);
    }
    return "redirect:/";
  }

  @GetMapping("/task/{parentId}/addSubTask")
  public String showAddSubTaskForm(@PathVariable Long parentId, Model model) {
    Optional<Todo> parentTodo = todoService.findTodoById(parentId);
    if (parentTodo.isPresent()) {
      model.addAttribute("parentTask", parentTodo.get());
      model.addAttribute("subtask", new Todo()); // For form binding
      model.addAttribute("priorities", Priority.values());
      return "add-subtask"; // Name of the new template for adding subtasks
    } else {
      // Parent task not found, redirect or show an error
      return "redirect:/";
    }
  }

  @PostMapping("/task/{parentId}/addSubTask")
  public String addSubTask(@PathVariable Long parentId,
                           @RequestParam String title,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                           @RequestParam(required = false) Priority priority) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.addSubTask(parentId, title, dueDate, priority);
    }
    return "redirect:/"; // Consider redirecting to parent task anchor: "/#task-" + parentId
  }

  @PostMapping("/todos/reorder")
  public ResponseEntity<Void> reorderTasks(@RequestBody ReorderRequestPayload payload) {
      if (payload == null || payload.getOrderedTaskIds() == null) {
          return ResponseEntity.badRequest().build(); // Basic validation
      }
      todoService.updateTaskOrder(payload.getOrderedTaskIds(), payload.getParentId());
      return ResponseEntity.ok().build();
  }
}
