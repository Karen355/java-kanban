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
    void shouldHaveUnlimitedHistoryWithoutDuplicates() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", Status.NEW);
            int taskId = taskManager.createTask(task);
            taskManager.getTaskById(taskId);
        }
        taskManager.getTaskById(5);
        taskManager.getTaskById(10);

        List<Task> history = taskManager.getHistory();
        assertEquals(15, history.size(), "История неограничена и без дубликатов");
        assertEquals(10, history.get(14).getId(), "Последний просмотр - задача 10, она в конце истории");
        assertEquals(5, history.get(13).getId(), "Предпоследний просмотр - задача 5");
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);
        taskManager.getTaskById(taskId);
        assertFalse(taskManager.getHistory().isEmpty());

        taskManager.deleteTaskById(taskId);
        assertTrue(taskManager.getHistory().stream().noneMatch(t -> t.getId() == taskId),
                "Удалённая задача не должна быть в истории");
    }

    @Test
    void shouldRemoveEpicAndSubtasksFromHistoryWhenEpicDeleted() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        int sub1 = taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        int sub2 = taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        taskManager.getEpicById(epicId);
        taskManager.getSubtaskById(sub1);
        taskManager.getSubtaskById(sub2);

        taskManager.deleteEpicById(epicId);
        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().noneMatch(t -> t.getId() == epicId), "Эпик должен быть удалён из истории");
        assertTrue(history.stream().noneMatch(t -> t.getId() == sub1), "Подзадача 1 должна быть удалена из истории");
        assertTrue(history.stream().noneMatch(t -> t.getId() == sub2), "Подзадача 2 должна быть удалена из истории");
    }

    @Test
    void deletedSubtaskIdsShouldNotRemainInEpic() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        int subId = taskManager.createSubtask(new Subtask("S", "D", Status.NEW, epicId));
        taskManager.deleteSubtaskById(subId);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertFalse(savedEpic.getSubtaskIds().contains(subId), "В эпике не должно оставаться id удалённой подзадачи");
    }

    @Test
    void shouldRemoveSubtasksFromHistoryWhenDeleteAllSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        int sub1 = taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        int sub2 = taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        taskManager.getSubtaskById(sub1);
        taskManager.getSubtaskById(sub2);

        taskManager.deleteAllSubtasks();
        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().noneMatch(t -> t.getId() == sub1), "Подзадачи должны быть удалены из истории");
        assertTrue(history.stream().noneMatch(t -> t.getId() == sub2), "Подзадачи должны быть удалены из истории");
    }

    @Test
    void epicShouldNotContainStaleSubtaskIdsAfterDeleteAllSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        taskManager.deleteAllSubtasks();

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "В эпике не должно оставаться id подзадач после deleteAllSubtasks");
    }

    @Test
    void getHistoryShouldReturnCopyNotInternalList() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);
        taskManager.getTaskById(taskId);
        List<Task> history1 = taskManager.getHistory();
        List<Task> history2 = taskManager.getHistory();
        assertNotSame(history1, history2, "getHistory должен возвращать новую копию списка");
    }

    @Test
    void changingTaskIdViaSetterBreaksManagerConsistency() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);
        Task retrieved = taskManager.getTaskById(taskId);
        retrieved.setId(999);

        assertNotNull(taskManager.getTaskById(taskId), "Менеджер хранит задачу по старому id");
        assertNull(taskManager.getTaskById(999), "По новому id задача не найдена - данные становятся несогласованными");
    }
}