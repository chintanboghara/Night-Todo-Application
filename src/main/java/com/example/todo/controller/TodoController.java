package com.example.todo.controller;

import com.example.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
  public String addTodo(@RequestParam("title") String title) {
    if (title != null && !title.trim().isEmpty()) {
      todoService.addTodo(title);
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
}
