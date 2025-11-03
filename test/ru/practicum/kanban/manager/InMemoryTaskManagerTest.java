package ru.practicum.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldAddAndFindTask() {
        Task task = new Task("Test", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldAddAndFindEpic() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
    }

    @Test
    void shouldAddAndFindSubtask() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    void shouldNotAddSubtaskWithInvalidEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 999);
        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(-1, subtaskId, "Подзадача не должна быть создана с несуществующим эпиком");
    }

    @Test
    void shouldUpdateEpicStatus() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask1);

        assertEquals(Status.NEW, taskManager.getEpicById(epicId).getStatus());

        Subtask subtaskFromManager = taskManager.getSubtaskById(subtaskId);
        subtaskFromManager.setStatus(Status.DONE);
        taskManager.updateSubtask(subtaskFromManager);

        assertEquals(Status.DONE, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);

        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task, history.get(0), "Первая задача в истории не совпадает");
        assertEquals(epic, history.get(1), "Вторая задача в истории не совпадает");
    }

    @Test
    void shouldLimitHistorySize() {
        // Создаем и просматриваем более 10 задач
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", Status.NEW);
            int taskId = taskManager.createTask(task);
            taskManager.getTaskById(taskId);
        }

        List<Task> history = taskManager.getHistory();
        assertTrue(history.size() <= 10, "История не должна превышать 10 элементов");
    }
}