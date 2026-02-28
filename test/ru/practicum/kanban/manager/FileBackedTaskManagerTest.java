package ru.practicum.kanban.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.practicum.kanban.model.*;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;

    @Override
    FileBackedTaskManager createManager() {
        return FileBackedTaskManager.loadFromFile(tempDir.resolve("tasks.csv"));
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Saved", "Desc", Status.NEW);
        taskManager.createTask(task);
        int taskId = task.getId();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempDir.resolve("tasks.csv"));
        Task loadedTask = loaded.getTaskById(taskId);
        assertNotNull(loadedTask);
        assertEquals("Saved", loadedTask.getTitle());
    }

    @Test
    void shouldThrowWhenSaveFails() {
        Path invalidPath = tempDir.resolve("nonexistent").resolve("tasks.csv");
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(invalidPath);
        assertThrows(ManagerSaveException.class, () -> manager.createTask(new Task("T", "D", Status.NEW)));
    }

    @Test
    void shouldNotThrowWhenFileDoesNotExistOnLoad() {
        Path newFile = tempDir.resolve("new_tasks.csv");
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(newFile));
    }

    @Test
    void shouldSaveAndLoadDurationAndStartTime() {
        Task task = new Task("With time", "Desc", Status.NEW);
        task.setDuration(Duration.ofMinutes(90));
        task.setStartTime(LocalDateTime.of(2025, 2, 20, 10, 0));
        taskManager.createTask(task);
        int taskId = task.getId();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempDir.resolve("tasks.csv"));
        Task loadedTask = loaded.getTaskById(taskId);
        assertNotNull(loadedTask);
        assertEquals(Duration.ofMinutes(90), loadedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 2, 20, 10, 0), loadedTask.getStartTime());
        assertEquals(LocalDateTime.of(2025, 2, 20, 11, 30), loadedTask.getEndTime());
    }
}
