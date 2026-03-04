package ru.practicum.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Базовые тесты для любой реализации TaskManager (InMemory, FileBacked).
 * ТЗ: абстрактный класс для избавления от дублирования кода в тестах менеджера.
 */
abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    abstract T createManager();

    @BeforeEach
    void setUp() {
        taskManager = createManager();
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
    void shouldAddAndFindSubtaskWithLinkedEpic() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
        assertEquals(epicId, savedSubtask.getEpicId(), "У подзадачи должен быть связанный эпик");
    }

    @Test
    void shouldNotAddSubtaskWithInvalidEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 999);
        int subtaskId = taskManager.createSubtask(subtask);
        assertEquals(-1, subtaskId, "Подзадача не должна быть создана с несуществующим эпиком");
    }

    @Test
    void shouldUpdateEpicStatusFromSubtasks() {
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
    void epicStatusWhenAllSubtasksNew() {
        Epic epic = new Epic("E", "D");
        int epicId = taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        assertEquals(Status.NEW, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void epicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic("E", "D");
        int epicId = taskManager.createEpic(epic);
        int s1 = taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        int s2 = taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        Subtask sub1 = taskManager.getSubtaskById(s1);
        sub1.setStatus(Status.DONE);
        taskManager.updateSubtask(sub1);
        Subtask sub2 = taskManager.getSubtaskById(s2);
        sub2.setStatus(Status.DONE);
        taskManager.updateSubtask(sub2);
        assertEquals(Status.DONE, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void epicStatusWhenSubtasksNewAndDone() {
        Epic epic = new Epic("E", "D");
        int epicId = taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask("S2", "D", Status.DONE, epicId));
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void epicStatusWhenSubtaskInProgress() {
        Epic epic = new Epic("E", "D");
        int epicId = taskManager.createEpic(epic);
        int s1 = taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        Subtask sub1 = taskManager.getSubtaskById(s1);
        sub1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
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
    void getPrioritizedTasksReturnsSortedByStartTime() {
        Task t1 = new Task("T1", "D", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));
        Task t2 = new Task("T2", "D", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 2, 19, 12, 0));
        t2.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(t1);
        taskManager.createTask(t2);
        List<Task> p = taskManager.getPrioritizedTasks();
        assertEquals(2, p.size());
        assertTrue(p.get(0).getStartTime().isBefore(p.get(1).getStartTime()));
    }

    @Test
    void tasksWithoutStartTimeNotInPrioritized() {
        taskManager.createTask(new Task("No time", "D", Status.NEW));
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void overlappingTaskRejected() {
        Task t1 = new Task("T1", "D", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(t1);
        Task t2 = new Task("T2", "D", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 30));
        t2.setDuration(Duration.ofMinutes(60));
        int id2 = taskManager.createTask(t2);
        assertEquals(-1, id2, "Пересекающаяся задача не должна быть создана");
    }

    @Test
    void epicDurationAndTimesFromSubtasks() {
        Epic epic = new Epic("E", "D");
        int epicId = taskManager.createEpic(epic);
        Subtask s1 = new Subtask("S1", "D", Status.NEW, epicId);
        s1.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 0));
        s1.setDuration(Duration.ofMinutes(30));
        Subtask s2 = new Subtask("S2", "D", Status.NEW, epicId);
        s2.setStartTime(LocalDateTime.of(2025, 2, 20, 11, 0));
        s2.setDuration(Duration.ofMinutes(60));
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);
        Epic saved = taskManager.getEpicById(epicId);
        assertEquals(Duration.ofMinutes(90), saved.getDuration());
        assertNotNull(saved.getStartTime());
        assertNotNull(saved.getEndTime());
    }

    @Test
    void getSubtasksByEpicIdReturnsOnlyThatEpicSubtasks() {
        Epic epic = new Epic("Epic", "D");
        int epicId = taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("S1", "D", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask("S2", "D", Status.NEW, epicId));
        List<Subtask> byEpic = taskManager.getSubtasksByEpicId(epicId);
        assertEquals(2, byEpic.size());
    }
}
