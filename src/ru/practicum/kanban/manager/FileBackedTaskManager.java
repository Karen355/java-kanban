package ru.practicum.kanban.manager;

import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер задач с сохранением состояния в файл.
 * Формат: строки id;type;title;description;status;durationMinutes;startTime;epicId,
 * пустая строка, затем id истории через ";".
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
        load();
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        return new FileBackedTaskManager(path);
    }

    private void load() {
        if (!Files.exists(path)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<Task> tasksToLoad = new ArrayList<>();
            List<Epic> epicsToLoad = new ArrayList<>();
            List<Subtask> subtasksToLoad = new ArrayList<>();
            List<Integer> historyIds = new ArrayList<>();
            String line;
            boolean emptySeen = false;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    emptySeen = true;
                    continue;
                }
                if (emptySeen) {
                    for (String idStr : line.split(";")) {
                        try {
                            historyIds.add(Integer.parseInt(idStr.trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                }
                Task task = fromString(line);
                if (task instanceof Epic) {
                    epicsToLoad.add((Epic) task);
                } else if (task instanceof Subtask) {
                    subtasksToLoad.add((Subtask) task);
                } else if (task != null) {
                    tasksToLoad.add(task);
                }
            }
            tasksToLoad.forEach(this::loadTask);
            epicsToLoad.forEach(this::loadEpic);
            subtasksToLoad.forEach(this::loadSubtask);
            if (!historyIds.isEmpty()) {
                loadHistory(historyIds);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
            writer.newLine();
            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                writer.write(history.stream()
                        .map(t -> String.valueOf(t.getId()))
                        .reduce((a, b) -> a + ";" + b)
                        .orElse(""));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        long durationMinutes = task.getDuration() != null ? task.getDuration().toMinutes() : -1;
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String epicIdStr = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return task.getId() + ";" + typeOf(task) + ";" + task.getTitle() + ";" + task.getDescription() + ";"
                + task.getStatus() + ";" + (durationMinutes >= 0 ? durationMinutes : "") + ";" + startTimeStr + ";" + epicIdStr;
    }

    private static String typeOf(Task task) {
        if (task instanceof Epic) return "EPIC";
        if (task instanceof Subtask) return "SUBTASK";
        return "TASK";
    }

    private Task fromString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 5) return null;
        int id = Integer.parseInt(parts[0].trim());
        String type = parts[1].trim();
        String title = parts[2].trim();
        String description = parts[3].trim();
        Status status = Status.valueOf(parts[4].trim());
        Duration duration = null;
        if (parts.length > 5 && !parts[5].trim().isEmpty()) {
            try {
                duration = Duration.ofMinutes(Long.parseLong(parts[5].trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        LocalDateTime startTime = null;
        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
            try {
                startTime = LocalDateTime.parse(parts[6].trim());
            } catch (Exception ignored) {
            }
        }
        switch (type) {
            case "TASK":
                Task task = new Task(title, description, status);
                task.setId(id);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case "EPIC":
                Epic epic = new Epic(title, description);
                epic.setId(id);
                return epic;
            case "SUBTASK":
                int epicId = parts.length > 7 && !parts[7].trim().isEmpty() ? Integer.parseInt(parts[7].trim()) : 0;
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                return null;
        }
    }

    @Override
    public int createTask(Task task) {
        int result = super.createTask(task);
        if (result != -1) save();
        return result;
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
    public int createEpic(Epic epic) {
        int result = super.createEpic(epic);
        save();
        return result;
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
    public int createSubtask(Subtask subtask) {
        int result = super.createSubtask(subtask);
        if (result != -1) save();
        return result;
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

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
