package com.example.todo.controller;

import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class TodoController {

  @Autowired
  private TodoService todoService;

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("todos", todoService.getTodos());
    return "index";
  }

  @PostMapping("/add")
  public String addTodo(@RequestParam("title") String title,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.addTodo(title, dueDate);
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
      return "edit-todo";
    } else {
      return "redirect:/";
    }
  }

  @PostMapping("/update/{id}")
  public String updateTodo(@PathVariable long id, @RequestParam String title,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.updateTodo(id, title, dueDate);
    }
    return "redirect:/";
  }
}
