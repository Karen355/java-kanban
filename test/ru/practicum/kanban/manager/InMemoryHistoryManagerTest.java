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
}