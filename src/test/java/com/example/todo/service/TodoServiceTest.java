package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    private Todo todo1;
    private Todo todo2;

    @BeforeEach
    void setUp() {
        todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Test Task 1");
        todo1.setCompleted(false);

        todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Test Task 2");
        todo2.setCompleted(true);
    }

    @Test
    void getTodos_shouldReturnListOfTodos() {
        when(todoRepository.findAll()).thenReturn(Arrays.asList(todo1, todo2));

        List<Todo> todos = todoService.getTodos();

        assertEquals(2, todos.size());
        assertEquals("Test Task 1", todos.get(0).getTitle());
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    void addTodo_shouldSaveAndReturnTodo() {
        Todo newTodo = new Todo();
        newTodo.setTitle("New Task");
        newTodo.setCompleted(false);

        // ArgumentCaptor can be used if we need to assert properties of the object passed to save
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        Todo savedTodo = todoService.addTodo("New Task");

        assertNotNull(savedTodo);
        assertEquals("New Task", savedTodo.getTitle());
        assertFalse(savedTodo.isCompleted());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void deleteTodo_shouldCallDeleteById() {
        doNothing().when(todoRepository).deleteById(1L);
        todoService.deleteTodo(1L);
        verify(todoRepository, times(1)).deleteById(1L);
    }

    @Test
    void markCompleted_shouldUpdateTodoToCompleted() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo1);

        todoService.markCompleted(1L);

        assertTrue(todo1.isCompleted());
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(todo1);
    }

    @Test
    void markCompleted_shouldDoNothingIfTodoNotFound() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());

        todoService.markCompleted(3L);

        verify(todoRepository, times(1)).findById(3L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void updateTodo_shouldUpdateTodoTitle() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo1);

        todoService.updateTodo(1L, "Updated Title");

        assertEquals("Updated Title", todo1.getTitle());
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(todo1);
    }

    @Test
    void updateTodo_shouldDoNothingIfTodoNotFound() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());

        todoService.updateTodo(3L, "Non Existent");

        verify(todoRepository, times(1)).findById(3L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void findTodoById_shouldReturnTodoWhenFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));

        Optional<Todo> foundTodo = todoService.findTodoById(1L);

        assertTrue(foundTodo.isPresent());
        assertEquals("Test Task 1", foundTodo.get().getTitle());
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    void findTodoById_shouldReturnEmptyOptionalWhenNotFound() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Todo> foundTodo = todoService.findTodoById(3L);

        assertFalse(foundTodo.isPresent());
        verify(todoRepository, times(1)).findById(3L);
    }
}
