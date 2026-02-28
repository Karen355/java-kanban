package ru.practicum.kanban.manager;

import ru.practicum.kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    /**
     * Задачи и подзадачи с заданным startTime, отсортированные по приоритету (без эпиков).
     */
    private final TreeSet<Task> prioritizedTasks;

    private static final Comparator<Task> PRIORITY_COMPARATOR =
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId);

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        prioritizedTasks = new TreeSet<>(PRIORITY_COMPARATOR);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().stream().filter(t -> t.getStartTime() != null).forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public int createTask(Task task) {
        if (task.getStartTime() != null && task.getEndTime() != null && taskOverlapsAny(task, getPrioritizedTasks())) {
            return -1;
        }
        int id = nextId++;
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return;
        }
        Task existing = tasks.get(task.getId());
        if (existing.getStartTime() != null) {
            prioritizedTasks.remove(existing);
        }
        if (task.getStartTime() != null && task.getEndTime() != null && taskOverlapsAny(task, getPrioritizedTasks())) {
            if (existing.getStartTime() != null) {
                prioritizedTasks.add(existing);
            }
            return;
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null && removed.getStartTime() != null) {
            prioritizedTasks.remove(removed);
        }
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        for (Subtask sub : subtasks.values()) {
            if (sub.getStartTime() != null) {
                prioritizedTasks.remove(sub);
            }
            historyManager.remove(sub.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = nextId++;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setTitle(epic.getTitle());
            existingEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Task sub = subtasks.remove(subtaskId);
                if (sub != null && sub.getStartTime() != null) {
                    prioritizedTasks.remove(sub);
                }
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask sub : subtasks.values()) {
            if (sub.getStartTime() != null) {
                prioritizedTasks.remove(sub);
            }
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return -1;
        }
        if (subtask.getStartTime() != null && subtask.getEndTime() != null && taskOverlapsAny(subtask, getPrioritizedTasks())) {
            return -1;
        }
        int id = nextId++;
        subtask.setId(id);
        subtasks.put(id, subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return;
        }
        Subtask existing = subtasks.get(subtask.getId());
        if (existing.getStartTime() != null) {
            prioritizedTasks.remove(existing);
        }
        if (subtask.getStartTime() != null && subtask.getEndTime() != null && taskOverlapsAny(subtask, getPrioritizedTasks())) {
            if (existing.getStartTime() != null) {
                prioritizedTasks.add(existing);
            }
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return List.of();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    /**
     * Проверка пересечения двух задач по времени (метод наложения отрезков: P1 < P4 && P2 > P3).
     */
    protected boolean tasksOverlap(Task a, Task b) {
        LocalDateTime startA = a.getStartTime();
        LocalDateTime endA = a.getEndTime();
        LocalDateTime startB = b.getStartTime();
        LocalDateTime endB = b.getEndTime();
        if (startA == null || endA == null || startB == null || endB == null) {
            return false;
        }
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    /**
     * Проверка, пересекается ли задача с любой другой в списке (исключая саму себя по id).
     */
    protected boolean taskOverlapsAny(Task task, List<Task> others) {
        return others.stream()
                .anyMatch(other -> other.getId() != task.getId() && tasksOverlap(task, other));
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            updateEpicTime(epicId);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != Status.NEW) {
                    allNew = false;
                }
                if (subtask.getStatus() != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
        updateEpicTime(epicId);
    }

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;
        List<Subtask> subs = getSubtasksByEpicId(epicId);
        if (subs.isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        long totalMinutes = subs.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        epic.setDuration(Duration.ofMinutes(totalMinutes));
        LocalDateTime start = subs.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime end = subs.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        epic.setStartTime(start);
        epic.setEndTime(end);
    }

    /**
     * Для загрузки из файла: восстановить задачу с заданным id без проверки пересечений.
     */
    protected void loadTask(Task task) {
        int id = task.getId();
        if (id >= nextId) nextId = id + 1;
        tasks.put(id, task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
    }

    /**
     * Для загрузки из файла: восстановить эпик с заданным id.
     */
    protected void loadEpic(Epic epic) {
        int id = epic.getId();
        if (id >= nextId) nextId = id + 1;
        epics.put(id, epic);
    }

    /**
     * Для загрузки из файла: восстановить подзадачу и обновить эпик.
     */
    protected void loadSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (id >= nextId) nextId = id + 1;
        subtasks.put(id, subtask);
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) epic.addSubtaskId(id);
        updateEpicStatus(subtask.getEpicId());
    }

    /**
     * Для загрузки из файла: восстановить историю просмотров по списку id.
     */
    protected void loadHistory(java.util.List<Integer> ids) {
        for (int id : ids) {
            Task t = tasks.get(id);
            if (t == null) t = epics.get(id);
            if (t == null) t = subtasks.get(id);
            if (t != null) historyManager.add(t);
        }
    }
}