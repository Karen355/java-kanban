package ru.practicum.kanban.manager;

import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;

import java.util.List;

// Не стал распределять файлы по отдельным папкам, так как нет требования в ТЗ.
public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    int createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);

    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();
}