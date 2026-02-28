package ru.practicum.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test", "Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.getFirst(), "Задачи в истории не совпадают");
    }

    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size(), "История должна быть пустой при добавлении null");
    }

    @Test
    void shouldLimitHistoryToTenTasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");

        // Проверяем, что остались последние 10 задач
        assertEquals(6, history.get(0).getId(), "Первая задача в истории должна быть с ID 6");
        assertEquals(15, history.get(9).getId(), "Последняя задача в истории должна быть с ID 15");
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasksViewed() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldMoveTaskToEndWhenDuplicateAdded() {
        Task task = new Task("Same", "D", Status.NEW);
        task.setId(1);
        Task other = new Task("Other", "D", Status.NEW);
        other.setId(2);
        historyManager.add(task);
        historyManager.add(other);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(1, history.get(1).getId());
    }

    @Test
    void shouldRemoveFromStart() {
        Task t1 = new Task("1", "D", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("2", "D", Status.NEW);
        t2.setId(2);
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(2, history.getFirst().getId());
    }

    @Test
    void shouldRemoveFromMiddle() {
        Task t1 = new Task("1", "D", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("2", "D", Status.NEW);
        t2.setId(2);
        Task t3 = new Task("3", "D", Status.NEW);
        t3.setId(3);
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void shouldRemoveFromEnd() {
        Task t1 = new Task("1", "D", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("2", "D", Status.NEW);
        t2.setId(2);
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(1, history.getFirst().getId());
    }
}