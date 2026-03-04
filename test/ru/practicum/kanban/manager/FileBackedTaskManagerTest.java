package ru.practicum.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File backupFile;

    @BeforeEach
    void setUp() throws IOException {
        backupFile = File.createTempFile("kanban", ".csv");
        backupFile.deleteOnExit();
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        FileBackedTaskManager manager = new FileBackedTaskManager(backupFile);
        manager.save();

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
        FileBackedTaskManager manager = new FileBackedTaskManager(backupFile);
        Task task1 = new Task("Task1", "Description task1", Status.NEW);
        Task task2 = new Task("Task2", "Description task2", Status.DONE);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        List<Task> tasks = loaded.getAllTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task1") && t.getStatus() == Status.NEW));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task2") && t.getStatus() == Status.DONE));
    }

    @Test
    void shouldSaveAndLoadTasksEpicsAndSubtasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(backupFile);
        manager.createTask(new Task("Task1", "Description task1", Status.NEW));
        int epicId = manager.createEpic(new Epic("Epic2", "Description epic2"));
        Epic epic = manager.getEpicById(epicId);
        epic.setStatus(Status.DONE);
        manager.updateEpic(epic);
        manager.createSubtask(new Subtask("Sub Task2", "Description sub task3", Status.DONE, epicId));

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
        FileBackedTaskManager manager = new FileBackedTaskManager(backupFile);
        int taskId = manager.createTask(new Task("Task", "Desc", Status.IN_PROGRESS));
        int epicId = manager.createEpic(new Epic("Epic", "Epic desc"));
        manager.createSubtask(new Subtask("Sub", "Sub desc", Status.NEW, epicId));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        assertEquals(manager.getTaskById(taskId).getTitle(), loaded.getTaskById(taskId).getTitle());
        assertEquals(manager.getEpicById(epicId).getSubtaskIds(), loaded.getEpicById(epicId).getSubtaskIds());

        loaded.deleteTaskById(taskId);
        assertNull(loaded.getTaskById(taskId));
        assertTrue(loaded.getAllTasks().isEmpty());
    }

    @Test
    void shouldGenerateNewIdsAfterLoad() {
        FileBackedTaskManager manager = new FileBackedTaskManager(backupFile);
        manager.createTask(new Task("A", "a", Status.NEW));
        manager.createTask(new Task("B", "b", Status.NEW));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(backupFile);
        int newId = loaded.createTask(new Task("C", "c", Status.NEW));
        assertEquals(3, newId);
        assertNotNull(loaded.getTaskById(3));
    }
}
