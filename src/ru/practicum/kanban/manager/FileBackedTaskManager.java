package ru.practicum.kanban.manager;

import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер задач с автосохранением состояния в CSV-файл.
 * Наследует логику от InMemoryTaskManager и после каждой модификации вызывает save().
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String CSV_HEADER = "id,type,name,status,description,epic,durationMinutes,startTime";

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        if (id != -1) save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        if (id != -1) save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    protected void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add(CSV_HEADER);
            for (Task task : getAllTasks()) {
                lines.add(taskToString(task));
            }
            for (Epic epic : getAllEpics()) {
                lines.add(taskToString(epic));
            }
            for (Subtask subtask : getAllSubtasks()) {
                lines.add(taskToString(subtask));
            }
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file, e);
        }
    }

    private static String taskToString(Task task) {
        TaskType type;
        String epicId = "";
        if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else {
            type = TaskType.TASK;
        }
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";
        return String.join(",",
                String.valueOf(task.getId()),
                type.name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr
        );
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Неверный формат строки: " + value);
        }
        int id = Integer.parseInt(parts[0].trim());
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2].trim();
        Status status = Status.valueOf(parts[3].trim());
        String description = parts[4].trim();
        String epicPart = parts.length > 5 ? parts[5].trim() : "";
        Duration duration = null;
        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
            try {
                duration = Duration.ofMinutes(Long.parseLong(parts[6].trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        LocalDateTime startTime = null;
        if (parts.length > 7 && !parts[7].trim().isEmpty()) {
            try {
                startTime = LocalDateTime.parse(parts[7].trim());
            } catch (DateTimeParseException ignored) {
            }
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = epicPart.isEmpty() ? 0 : Integer.parseInt(epicPart);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Восстанавливает менеджер из файла. Порядок: эпики, задачи, подзадачи.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + file, e);
        }
        String[] lines = content.split("\n");
        if (lines.length <= 1) {
            manager.setNextId(1);
            return manager;
        }
        List<Task> epics = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        List<Task> subtasks = new ArrayList<>();
        int maxId = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            Task task = fromString(line);
            maxId = Math.max(maxId, task.getId());
            if (task instanceof Epic) {
                epics.add(task);
            } else if (task instanceof Subtask) {
                subtasks.add(task);
            } else {
                tasks.add(task);
            }
        }
        for (Task t : epics) {
            manager.restoreEpic((Epic) t);
        }
        for (Task t : tasks) {
            manager.restoreTask(t);
            manager.addToPrioritizedIfNeeded(t);
        }
        for (Task t : subtasks) {
            manager.restoreSubtask((Subtask) t);
            manager.addToPrioritizedIfNeeded(t);
        }
        for (Epic epic : manager.getAllEpics()) {
            manager.updateEpicStatus(epic.getId());
        }
        manager.setNextId(maxId + 1);
        return manager;
    }

    /**
     * Дополнительное задание: сценарий сохранения и загрузки менеджера из файла.
     */
    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("kanban", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task1 = new Task("Task1", "Description task1", Status.NEW);
        Epic epic2 = new Epic("Epic2", "Description epic2");
        manager.createTask(task1);
        int epic2Id = manager.createEpic(epic2);
        epic2.setStatus(Status.DONE);
        manager.updateEpic(epic2);
        Subtask sub3 = new Subtask("Sub Task2", "Description sub task3", Status.DONE, epic2Id);
        manager.createSubtask(sub3);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assert loaded.getAllTasks().size() == 1;
        assert loaded.getAllEpics().size() == 1;
        assert loaded.getAllSubtasks().size() == 1;
        Task loadedTask = loaded.getTaskById(1);
        assert loadedTask != null && loadedTask.getTitle().equals("Task1");
        Epic loadedEpic = loaded.getEpicById(2);
        assert loadedEpic != null && loadedEpic.getStatus() == Status.DONE;
        Subtask loadedSub = loaded.getSubtaskById(3);
        assert loadedSub != null && loadedSub.getEpicId() == 2;

        System.out.println("Сценарий сохранения и загрузки выполнен успешно.");
    }
}
