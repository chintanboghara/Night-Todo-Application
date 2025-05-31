package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {
  private final TodoRepository todoRepository;

  @Autowired
  public TodoService(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  public List<Todo> getTodos() {
    return todoRepository.findAll();
  }

  public Todo addTodo(String title, LocalDate dueDate) {
    Todo todo = new Todo();
    todo.setTitle(title);
    todo.setCompleted(false);
    todo.setDueDate(dueDate);
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

  public Optional<Todo> updateTodo(long id, String title, LocalDate dueDate) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    return optionalTodo.map(todo -> {
      todo.setTitle(title);
      todo.setDueDate(dueDate);
      return todoRepository.save(todo);
    });
  }

  public Optional<Todo> findTodoById(long id) {
    return todoRepository.findById(id);
  }
}
