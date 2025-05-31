package com.example.todo.service;

import com.example.todo.model.Priority;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList; // Added this import
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

    // ArgumentCaptors for Specification and Sort to be used in multiple tests
    private ArgumentCaptor<Specification<Todo>> specCaptor;
    private ArgumentCaptor<Sort> sortCaptor;
    private Todo todo1; // Keep for existing tests, can be removed if those tests are refactored
    private Todo todo2; // Keep for existing tests

    @BeforeEach
    void setUp() {
        specCaptor = ArgumentCaptor.forClass(Specification.class);
        sortCaptor = ArgumentCaptor.forClass(Sort.class);
        // Common stubbing for findAll for getTodos tests
        // Individual tests can override this if they need to check the returned list
        when(todoRepository.findAll(specCaptor.capture(), sortCaptor.capture())).thenReturn(new ArrayList<>());

        // Setup for existing tests, can be refactored if those tests change significantly
        todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Test Task 1");
        todo1.setCompleted(false);
        todo1.setPriority(Priority.MEDIUM);


        todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Test Task 2");
        todo2.setCompleted(true);
        todo2.setPriority(Priority.HIGH);
    }

    // --- Tests for getTodos ---

    @Test
    void getTodos_defaultParameters_shouldSortByCreationDateAsc() {
        todoService.getTodos(null, null, null, null, null, null); // All null or default

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("creationDate").getDirection());
        assertNotNull(specCaptor.getValue());
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_explicitDefaultParameters_shouldSortByCreationDateAsc() {
        todoService.getTodos("ALL", null, "ALL", "", "creationDate", "ASC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("creationDate").getDirection());
        assertNotNull(specCaptor.getValue());
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByTitleDesc_shouldApplyCorrectSort() {
        todoService.getTodos(null, null, null, null, "title", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("title").getDirection());
        assertNull(capturedSort.getOrderFor("creationDate"));
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByDueDateAsc_shouldApplyCorrectSort() {
        todoService.getTodos(null, null, null, null, "dueDate", "ASC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("dueDate").getDirection());
        assertNull(capturedSort.getOrderFor("creationDate"));
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByPriorityDesc_shouldApplyCorrectSort() {
        todoService.getTodos(null, null, null, null, "priority", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("priority").getDirection());
        assertNull(capturedSort.getOrderFor("creationDate"));
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_invalidSortBy_shouldDefaultToCreationDateWithProvidedDirection() {
        todoService.getTodos(null, null, null, null, "invalidField", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("creationDate").getDirection());
        assertNull(capturedSort.getOrderFor("invalidField"));
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_invalidSortDir_shouldDefaultToAscForSpecifiedField() {
        todoService.getTodos(null, null, null, null, "title", "INVALID_DIR");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("title").getDirection());
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    // Tests for filters (focus on repository call, not spec details)
    @Test
    void getTodos_withFilterByStatusCompleted_callsRepository() {
        todoService.getTodos("COMPLETED", null, null, null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByStatusPending_callsRepository() {
        todoService.getTodos("PENDING", null, null, null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByPriorityHigh_callsRepository() {
        todoService.getTodos(null, Priority.HIGH, null, null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByDueDateOverdue_callsRepository() {
        todoService.getTodos(null, null, "OVERDUE", null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByDueDateToday_callsRepository() {
        todoService.getTodos(null, null, "TODAY", null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByDueDateNext7Days_callsRepository() {
        todoService.getTodos(null, null, "NEXT_7_DAYS", null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByDueDateSpecificDate_callsRepository() {
        todoService.getTodos(null, null, "2024-01-15", null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withFilterByDueDateInvalidDateKeyword_callsRepository() {
        todoService.getTodos(null, null, "INVALID_KEYWORD", null, null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withSearchTerm_callsRepository() {
        todoService.getTodos(null, null, null, "my search term", null, null);
        verify(todoRepository).findAll(specCaptor.capture(), any(Sort.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTodos_withCombinedFiltersAndSort_callsRepositoryWithCorrectSort() {
        // Reset captors for this specific call to ensure clean capture
        specCaptor = ArgumentCaptor.forClass(Specification.class);
        sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(todoRepository.findAll(specCaptor.capture(), sortCaptor.capture())).thenReturn(new ArrayList<>());

        todoService.getTodos("PENDING", Priority.MEDIUM, "TODAY", "search", "priority", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("priority").getDirection());
        assertNotNull(specCaptor.getValue());
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    // --- End of tests for getTodos ---

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

    // Tests for addSubTask
    @Test
    void addSubTask_shouldCreateAndReturnSubtask_whenParentExists() {
        Long parentId = 1L;
        String title = "Subtask Title";
        LocalDate dueDate = LocalDate.now().plusDays(2);
        Priority priority = Priority.HIGH;

        Todo parentTodo = new Todo();
        parentTodo.setId(parentId);
        parentTodo.setTitle("Parent Task");

        when(todoRepository.findById(parentId)).thenReturn(Optional.of(parentTodo));

        Todo subtaskToSave = new Todo(); // Will have default MEDIUM priority
        subtaskToSave.setTitle(title);
        subtaskToSave.setDueDate(dueDate);
        subtaskToSave.setPriority(priority); // Explicitly set for this test
        subtaskToSave.setParent(parentTodo);
        // parentTodo.getSubTasks().add(subtaskToSave); // Simulating bidirectional link if addSubTask utility was used

        when(todoRepository.save(any(Todo.class))).thenReturn(subtaskToSave);

        Optional<Todo> savedSubtaskOptional = todoService.addSubTask(parentId, title, dueDate, priority);

        assertTrue(savedSubtaskOptional.isPresent());
        Todo savedSubtask = savedSubtaskOptional.get();
        assertEquals(title, savedSubtask.getTitle());
        assertEquals(dueDate, savedSubtask.getDueDate());
        assertEquals(priority, savedSubtask.getPriority());
        assertNotNull(savedSubtask.getParent());
        assertEquals(parentId, savedSubtask.getParent().getId());

        verify(todoRepository, times(1)).findById(parentId);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void addSubTask_shouldReturnEmpty_whenParentNotFound() {
        Long parentId = 99L; // Non-existent parent
        when(todoRepository.findById(parentId)).thenReturn(Optional.empty());

        Optional<Todo> result = todoService.addSubTask(parentId, "Subtask Title", null, Priority.MEDIUM);

        assertFalse(result.isPresent());
        verify(todoRepository, times(1)).findById(parentId);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void addSubTask_whenNullPriority_shouldDefaultToMedium() {
        Long parentId = 1L;
        String title = "Subtask Default Priority";
        LocalDate dueDate = LocalDate.now().plusDays(2);
        Todo parentTodo = new Todo();
        parentTodo.setId(parentId);

        when(todoRepository.findById(parentId)).thenReturn(Optional.of(parentTodo));

        // Capture the argument passed to save to check its priority
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo saved = invocation.getArgument(0);
            assertEquals(Priority.MEDIUM, saved.getPriority());
            return saved;
        });

        Optional<Todo> savedSubtaskOptional = todoService.addSubTask(parentId, title, dueDate, null);

        assertTrue(savedSubtaskOptional.isPresent());
        assertEquals(Priority.MEDIUM, savedSubtaskOptional.get().getPriority());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }
}
