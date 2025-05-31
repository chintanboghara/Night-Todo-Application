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
    void getTodos_defaultParameters_shouldSortByDisplayOrderAsc() {
        todoService.getTodos(null, null, null, null, null, null); // All null for sortBy and sortDir

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection());
        assertNotNull(specCaptor.getValue());
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByNullOrEmptyOrManual_shouldSortByDisplayOrder() {
        todoService.getTodos(null, null, null, null, null, "DESC"); // sortBy null, sortDir DESC
        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("displayOrder").getDirection());

        todoService.getTodos(null, null, null, null, "", "ASC"); // sortBy empty, sortDir ASC
        capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection());

        todoService.getTodos(null, null, null, null, "manual", "DESC"); // sortBy "manual"
        capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("displayOrder").getDirection());

        todoService.getTodos(null, null, null, null, "displayOrder", "ASC"); // sortBy "displayOrder"
        capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection());
    }

    @Test
    void getTodos_sortByTitleDesc_shouldApplyCorrectSortWithSecondaryDisplayOrder() {
        todoService.getTodos(null, null, null, null, "title", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("title").getDirection());
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection()); // Secondary sort
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByDueDateAsc_shouldApplyCorrectSortWithSecondaryDisplayOrder() {
        todoService.getTodos(null, null, null, null, "dueDate", "ASC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("dueDate").getDirection());
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection()); // Secondary sort
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_sortByCreationDateAsc_shouldApplyCorrectSortWithSecondaryDisplayOrder() {
        todoService.getTodos(null, null, null, null, "creationDate", "ASC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("creationDate").getDirection());
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection()); // Secondary sort
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }


    @Test
    void getTodos_sortByPriorityDesc_shouldApplyCorrectSortWithSecondaryDisplayOrder() {
        todoService.getTodos(null, null, null, null, "priority", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("priority").getDirection());
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection()); // Secondary sort
        verify(todoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getTodos_invalidSortBy_shouldDefaultToDisplayOrderWithProvidedDirection() {
        todoService.getTodos(null, null, null, null, "invalidField", "DESC");

        Sort capturedSort = sortCaptor.getValue();
        // Default field is "displayOrder" if primary sort field is invalid
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("displayOrder").getDirection());
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
        // Reset captors for this specific call to ensure clean capture for this test's specific when()
        ArgumentCaptor<Specification<Todo>> localSpecCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Sort> localSortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(todoRepository.findAll(localSpecCaptor.capture(), localSortCaptor.capture())).thenReturn(new ArrayList<>());

        todoService.getTodos("PENDING", Priority.MEDIUM, "TODAY", "search", "priority", "DESC");

        Sort capturedSort = localSortCaptor.getValue(); // Use local captor
        assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("priority").getDirection());
        assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("displayOrder").getDirection()); // Secondary sort
        assertNotNull(localSpecCaptor.getValue());
        verify(todoRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    // --- End of tests for getTodos ---

    // --- Tests for displayOrder in addTodo and addSubTask ---
    @Test
    void addTodo_shouldSetCorrectDisplayOrder_forNewTopLevelTask() {
        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        when(todoRepository.save(todoCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // First task
        when(todoRepository.findMaxDisplayOrderByParentIsNull()).thenReturn(null);
        todoService.addTodo("First Task", null, null);
        assertEquals(0, todoCaptor.getValue().getDisplayOrder());

        // Subsequent task
        when(todoRepository.findMaxDisplayOrderByParentIsNull()).thenReturn(1);
        todoService.addTodo("Third Task", null, null);
        assertEquals(2, todoCaptor.getValue().getDisplayOrder());
    }

    @Test
    void addSubTask_shouldSetCorrectDisplayOrder_forNewSubTask() {
        Long parentId = 1L;
        Todo parentTodo = new Todo();
        parentTodo.setId(parentId);
        when(todoRepository.findById(parentId)).thenReturn(Optional.of(parentTodo));

        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        when(todoRepository.save(todoCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // First subtask for this parent
        when(todoRepository.findMaxDisplayOrderByParent(parentTodo)).thenReturn(null);
        todoService.addSubTask(parentId, "First Subtask", null, null);
        assertEquals(0, todoCaptor.getValue().getDisplayOrder());
        assertEquals(parentTodo, todoCaptor.getValue().getParent());

        // Subsequent subtask for this parent
        when(todoRepository.findMaxDisplayOrderByParent(parentTodo)).thenReturn(0);
        todoService.addSubTask(parentId, "Second Subtask", null, null);
        assertEquals(1, todoCaptor.getValue().getDisplayOrder());
        assertEquals(parentTodo, todoCaptor.getValue().getParent());
    }

    // --- Tests for updateTaskOrder ---
    @Test
    void updateTaskOrder_shouldReorderTopLevelTasks() {
        Todo t1 = new Todo(); t1.setId(1L); t1.setDisplayOrder(0); t1.setParent(null);
        Todo t2 = new Todo(); t2.setId(2L); t2.setDisplayOrder(1); t2.setParent(null);
        Todo t3 = new Todo(); t3.setId(3L); t3.setDisplayOrder(2); t3.setParent(null);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(todoRepository.findById(2L)).thenReturn(Optional.of(t2));
        when(todoRepository.findById(3L)).thenReturn(Optional.of(t3));

        ArgumentCaptor<List<Todo>> saveAllCaptor = ArgumentCaptor.forClass(List.class);

        todoService.updateTaskOrder(Arrays.asList(3L, 1L, 2L), null);

        verify(todoRepository).saveAll(saveAllCaptor.capture());
        List<Todo> savedTasks = saveAllCaptor.getValue();

        assertEquals(3, savedTasks.size());
        assertEquals(0, savedTasks.stream().filter(t -> t.getId() == 3L).findFirst().get().getDisplayOrder());
        assertEquals(1, savedTasks.stream().filter(t -> t.getId() == 1L).findFirst().get().getDisplayOrder());
        assertEquals(2, savedTasks.stream().filter(t -> t.getId() == 2L).findFirst().get().getDisplayOrder());
    }

    @Test
    void updateTaskOrder_shouldReorderSubtasks_forSpecificParent() {
        Todo parent = new Todo(); parent.setId(10L);
        Todo s1 = new Todo(); s1.setId(1L); s1.setDisplayOrder(0); s1.setParent(parent);
        Todo s2 = new Todo(); s2.setId(2L); s2.setDisplayOrder(1); s2.setParent(parent);
        Todo s3 = new Todo(); s3.setId(3L); s3.setDisplayOrder(2); s3.setParent(parent);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(s1));
        when(todoRepository.findById(2L)).thenReturn(Optional.of(s2));
        when(todoRepository.findById(3L)).thenReturn(Optional.of(s3));

        ArgumentCaptor<List<Todo>> saveAllCaptor = ArgumentCaptor.forClass(List.class);

        todoService.updateTaskOrder(Arrays.asList(3L, 1L, 2L), 10L);

        verify(todoRepository).saveAll(saveAllCaptor.capture());
        List<Todo> savedTasks = saveAllCaptor.getValue();

        assertEquals(3, savedTasks.size());
        assertEquals(0, savedTasks.stream().filter(t -> t.getId() == 3L).findFirst().get().getDisplayOrder());
        assertEquals(1, savedTasks.stream().filter(t -> t.getId() == 1L).findFirst().get().getDisplayOrder());
        assertEquals(2, savedTasks.stream().filter(t -> t.getId() == 2L).findFirst().get().getDisplayOrder());
    }

    @Test
    void updateTaskOrder_shouldOnlyUpdateTasksMatchingParentContext_andSkipUnchangedOrders() {
        Todo t1 = new Todo(); t1.setId(1L); t1.setDisplayOrder(0); t1.setParent(null); // Matches parentId = null
        Todo p1 = new Todo(); p1.setId(10L);
        Todo s1 = new Todo(); s1.setId(2L); s1.setDisplayOrder(1); s1.setParent(p1); // Does not match parentId = null
        Todo t2 = new Todo(); t2.setId(3L); t2.setDisplayOrder(1); t2.setParent(null); // Matches, but order is already correct for its new position if it were [t1,t2]

        when(todoRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(todoRepository.findById(2L)).thenReturn(Optional.of(s1)); // s1 will be skipped due to parent mismatch
        when(todoRepository.findById(3L)).thenReturn(Optional.of(t2));


        ArgumentCaptor<List<Todo>> saveAllCaptor = ArgumentCaptor.forClass(List.class);

        // Attempt to reorder [t1, s1, t2] as top-level. s1 should be ignored.
        // If t1 is now at index 0 (new order 0), and its old order was 0, it's skipped by saveAll.
        // If t2 is now at index 1 (new order 1, after s1 is filtered out conceptually), and its old order was 1, it's also skipped.
        // Let's make one of them change order: reorder [t2, t1] as top-level
        // t2 (id 3) new order 0, old order 1 -> should be updated
        // t1 (id 1) new order 1, old order 0 -> should be updated
        todoService.updateTaskOrder(Arrays.asList(3L, 1L), null);

        verify(todoRepository).saveAll(saveAllCaptor.capture());
        List<Todo> savedTasks = saveAllCaptor.getValue();

        assertEquals(2, savedTasks.size()); // Only t1 and t2 should be considered for update
        assertTrue(savedTasks.stream().anyMatch(t -> t.getId() == 1L && t.getDisplayOrder() == 1));
        assertTrue(savedTasks.stream().anyMatch(t -> t.getId() == 3L && t.getDisplayOrder() == 0));

        // Test case where order doesn't change for any matched task
        reset(todoRepository); // Reset mocks for new verification
        t1.setDisplayOrder(0); // Reset to original for this part of test
        t2.setDisplayOrder(1);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(todoRepository.findById(3L)).thenReturn(Optional.of(t2));

        todoService.updateTaskOrder(Arrays.asList(1L, 3L), null); // Orders match existing orders
        verify(todoRepository, never()).saveAll(any()); // saveAll should not be called
    }


    @Test
    void addTodo_shouldSaveAndReturnTodoWithPriority() {
        LocalDate dueDate = LocalDate.now().plusDays(5);
        Priority priority = Priority.HIGH;

        // Mock findMaxDisplayOrderByParentIsNull for this specific test, as it's called by addTodo
        when(todoRepository.findMaxDisplayOrderByParentIsNull()).thenReturn(null);

        Todo todoToSave = new Todo();
        todoToSave.setTitle("New Task");
        todoToSave.setCompleted(false);
        todoToSave.setDueDate(dueDate);
        todoToSave.setPriority(priority);
        todoToSave.setDisplayOrder(0); // What we expect it to be set to

        // Capture the argument to save, then return it, checking displayOrder
        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        when(todoRepository.save(todoCaptor.capture())).thenReturn(todoToSave);


        Todo savedTodo = todoService.addTodo("New Task", dueDate, priority);

        assertNotNull(savedTodo);
        assertEquals("New Task", savedTodo.getTitle());
        assertFalse(savedTodo.isCompleted());
        assertEquals(dueDate, savedTodo.getDueDate());
        assertEquals(priority, savedTodo.getPriority());
        assertEquals(0, savedTodo.getDisplayOrder()); // Verify displayOrder
        verify(todoRepository, times(1)).save(any(Todo.class));
        verify(todoRepository, times(1)).findMaxDisplayOrderByParentIsNull();

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
