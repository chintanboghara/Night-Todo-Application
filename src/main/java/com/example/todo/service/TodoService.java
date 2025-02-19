package com.example.todo.service;

import com.example.todo.model.Todo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TodoService {
  private final List<Todo> todos = new ArrayList<>();
  private final AtomicLong idCounter = new AtomicLong();

  public List<Todo> getTodos() {
    return todos;
  }

  public Todo addTodo(String title) {
    Todo todo = new Todo(idCounter.incrementAndGet(), title, false);
    todos.add(todo);
    return todo;
  }

  public void deleteTodo(long id) {
    todos.removeIf(todo -> todo.getId() == id);
  }

  public void markCompleted(long id) {
    todos.stream()
         .filter(todo -> todo.getId() == id)
         .findFirst()
         .ifPresent(todo -> todo.setCompleted(true));
  }
}
