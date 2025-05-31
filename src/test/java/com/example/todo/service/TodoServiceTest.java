package com.example.todo.service;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    void addTodo_shouldSaveAndReturnTodoWithPriority() {
        LocalDate dueDate = LocalDate.now().plusDays(5);
        Priority priority = Priority.HIGH;

        Todo todoToSave = new Todo(); // This will have default priority MEDIUM
        todoToSave.setTitle("New Task");
        todoToSave.setCompleted(false);
        todoToSave.setDueDate(dueDate);
        todoToSave.setPriority(priority); // Explicitly set for this test case

        when(todoRepository.save(any(Todo.class))).thenReturn(todoToSave);

        Todo savedTodo = todoService.addTodo("New Task", dueDate, priority);

        assertNotNull(savedTodo);
        assertEquals("New Task", savedTodo.getTitle());
        assertFalse(savedTodo.isCompleted());
        assertEquals(dueDate, savedTodo.getDueDate());
        assertEquals(priority, savedTodo.getPriority());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void addTodo_whenNullPriority_shouldDefaultToMedium() {
        LocalDate dueDate = LocalDate.now().plusDays(5);

        // We expect the service to create a Todo which will use its default constructor
        // to set Priority.MEDIUM, or the service itself ensures MEDIUM if null is passed.
        // The current service logic for addTodo: if (priority != null) { todo.setPriority(priority); }
        // else it remains what the default constructor set (which is MEDIUM).

        Todo todoWithDefaultPriority = new Todo(); // Uses default constructor, priority is MEDIUM
        todoWithDefaultPriority.setTitle("Default Priority Task");
        todoWithDefaultPriority.setDueDate(dueDate);
        // No need to set priority here, it's MEDIUM by default from constructor

        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo saved = invocation.getArgument(0);
            // Simulate what repository save would do if it returned the actual saved entity
            // For this test, we're interested in the state of 'saved' as prepared by the service
            assertEquals(Priority.MEDIUM, saved.getPriority());
            return saved;
        });

        Todo savedTodo = todoService.addTodo("Default Priority Task", dueDate, null);

        assertNotNull(savedTodo);
        assertEquals("Default Priority Task", savedTodo.getTitle());
        assertEquals(Priority.MEDIUM, savedTodo.getPriority()); // Verify it defaulted to MEDIUM
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
    void updateTodo_shouldUpdateTodoTitleDueDateAndPriority() {
        LocalDate newDueDate = LocalDate.now().plusDays(10);
        Priority newPriority = Priority.LOW;

        todo1.setPriority(Priority.HIGH); // Set an initial priority different from newPriority

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Todo> updatedTodoOptional = todoService.updateTodo(1L, "Updated Title", newDueDate, newPriority);

        assertTrue(updatedTodoOptional.isPresent());
        Todo updatedTodo = updatedTodoOptional.get();
        assertEquals("Updated Title", updatedTodo.getTitle());
        assertEquals(newDueDate, updatedTodo.getDueDate());
        assertEquals(newPriority, updatedTodo.getPriority());

        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void updateTodo_whenNullPriority_shouldRetainExistingPriority() {
        LocalDate newDueDate = LocalDate.now().plusDays(10);
        Priority initialPriority = Priority.HIGH;
        todo1.setPriority(initialPriority);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Todo> updatedTodoOptional = todoService.updateTodo(1L, "Updated Title", newDueDate, null);

        assertTrue(updatedTodoOptional.isPresent());
        Todo updatedTodo = updatedTodoOptional.get();
        assertEquals("Updated Title", updatedTodo.getTitle());
        assertEquals(newDueDate, updatedTodo.getDueDate());
        assertEquals(initialPriority, updatedTodo.getPriority()); // Priority should not change

        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void updateTodo_shouldDoNothingIfTodoNotFound() {
        LocalDate newDueDate = LocalDate.now().plusDays(10);
        Priority newPriority = Priority.HIGH;
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Todo> updatedTodoOptional = todoService.updateTodo(3L, "Non Existent", newDueDate, newPriority);

        assertFalse(updatedTodoOptional.isPresent());
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
