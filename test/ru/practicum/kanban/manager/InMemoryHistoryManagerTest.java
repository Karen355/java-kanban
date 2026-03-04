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
    void shouldNotContainDuplicatesAndKeepLastView() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории не должно быть дубликатов");
        assertEquals(task2, history.get(0), "Первой должна быть задача 2");
        assertEquals(task1, history.get(1), "Последний просмотр задачи 1 должен быть в конце");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task = new Task("Test", "Description", Status.NEW);
        task.setId(1);
        historyManager.add(task);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void shouldRemoveTaskFromMiddleOfHistory() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldHaveUnlimitedHistorySize() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(15, history.size(), "История должна содержать все 15 задач без ограничения");
        for (int i = 0; i < 15; i++) {
            assertEquals(i + 1, history.get(i).getId());
        }
    }

    @Test
    void shouldNotThrowWhenRemovingNonexistentId() {
        Task t = new Task("T", "D", Status.NEW);
        t.setId(1);
        historyManager.add(t);
        assertDoesNotThrow(() -> historyManager.remove(999));
        assertEquals(1, historyManager.getHistory().size());
    }
}
