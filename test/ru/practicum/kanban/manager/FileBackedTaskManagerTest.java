package ru.practicum.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.exception.NotFoundException;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File backupFile;

    @Override
    @BeforeEach
    void setUp() {
        try {
            backupFile = File.createTempFile("kanban", ".csv");
            backupFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.setUp();
    }

    @Override
    FileBackedTaskManager createManager() {
        return FileBackedTaskManager.loadFromFile(backupFile);
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        taskManager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldLoadEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("kanban_empty", ".csv");
        emptyFile.deleteOnExit();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(emptyFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadSeveralTasks() {
        Task task1 = new Task("Task1", "Description task1", Status.NEW);
        Task task2 = new Task("Task2", "Description task2", Status.DONE);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        List<Task> tasks = loaded.getAllTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task1") && t.getStatus() == Status.NEW));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task2") && t.getStatus() == Status.DONE));
    }

    @Test
    void shouldSaveAndLoadTasksEpicsAndSubtasks() {
        taskManager.createTask(new Task("Task1", "Description task1", Status.NEW));
        int epicId = taskManager.createEpic(new Epic("Epic2", "Description epic2"));
        Epic epic = taskManager.getEpicById(epicId);
        epic.setStatus(Status.DONE);
        taskManager.updateEpic(epic);
        taskManager.createSubtask(new Subtask("Sub Task2", "Description sub task3", Status.DONE, epicId));
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());
        Task loadedTask = loaded.getTaskById(1);
        assertNotNull(loadedTask);
        assertEquals("Task1", loadedTask.getTitle());
        assertEquals(Status.NEW, loadedTask.getStatus());
        Epic loadedEpic = loaded.getEpicById(2);
        assertNotNull(loadedEpic);
        assertEquals("Epic2", loadedEpic.getTitle());
        assertEquals(Status.DONE, loadedEpic.getStatus());
        assertEquals(1, loadedEpic.getSubtaskIds().size());
        Subtask loadedSub = loaded.getSubtaskById(3);
        assertNotNull(loadedSub);
        assertEquals("Sub Task2", loadedSub.getTitle());
        assertEquals(2, loadedSub.getEpicId());
    }

    @Test
    void shouldWorkLikeInMemoryManagerAfterLoad() {
        int taskId = taskManager.createTask(new Task("Task", "Desc", Status.IN_PROGRESS));
        int epicId = taskManager.createEpic(new Epic("Epic", "Epic desc"));
        taskManager.createSubtask(new Subtask("Sub", "Sub desc", Status.NEW, epicId));
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        assertEquals(taskManager.getTaskById(taskId).getTitle(), loaded.getTaskById(taskId).getTitle());
        assertEquals(taskManager.getEpicById(epicId).getSubtaskIds(), loaded.getEpicById(epicId).getSubtaskIds());
        loaded.deleteTaskById(taskId);
        assertThrows(NotFoundException.class, () -> loaded.getTaskById(taskId));
        assertTrue(loaded.getAllTasks().isEmpty());
    }

    @Test
    void shouldGenerateNewIdsAfterLoad() {
        taskManager.createTask(new Task("A", "a", Status.NEW));
        taskManager.createTask(new Task("B", "b", Status.NEW));
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        int newId = loaded.createTask(new Task("C", "c", Status.NEW));
        assertEquals(3, newId);
        assertNotNull(loaded.getTaskById(3));
    }

    @Test
    void shouldSaveAndLoadDurationAndStartTime() {
        Task task = new Task("With time", "Desc", Status.NEW);
        task.setDuration(Duration.ofMinutes(90));
        task.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 0));
        taskManager.createTask(task);
        int id = task.getId();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        Task loadedTask = loaded.getTaskById(id);
        assertNotNull(loadedTask);
        assertEquals(Duration.ofMinutes(90), loadedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 2, 20, 10, 0), loadedTask.getStartTime());
        assertEquals(LocalDateTime.of(2025, 2, 20, 11, 30), loadedTask.getEndTime());
    }

    @Test
    void shouldThrowWhenSaveToNonexistentDirectory() {
        File invalidPath = new File("nonexistent_dir_xyz", "tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(invalidPath);
        assertThrows(ManagerSaveException.class, manager::save,
                "Сохранение в несуществующую директорию должно приводить к исключению");
    }
}
