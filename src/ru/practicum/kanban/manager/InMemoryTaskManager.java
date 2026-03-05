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
        for (Task t : tasks.values()) {
            historyManager.remove(t.getId());
            if (t.getStartTime() != null) prioritizedTasks.remove(t);
        }
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
        if (task.getStartTime() != null && task.getEndTime() != null && taskOverlapsAny(task)) {
            return -1;
        }
        int id = nextId++;
        task.setId(id);
        tasks.put(id, task);
        addToPrioritizedIfNeeded(task);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;
        Task existing = tasks.get(task.getId());
        if (existing.getStartTime() != null) prioritizedTasks.remove(existing);
        if (task.getStartTime() != null && task.getEndTime() != null && taskOverlapsAny(task)) {
            addToPrioritizedIfNeeded(existing);
            return;
        }
        tasks.put(task.getId(), task);
        addToPrioritizedIfNeeded(task);
    }

    @Override
    public void deleteTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null && removed.getStartTime() != null) prioritizedTasks.remove(removed);
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
            if (sub.getStartTime() != null) prioritizedTasks.remove(sub);
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
                if (sub != null && sub.getStartTime() != null) prioritizedTasks.remove(sub);
                historyManager.remove(subtaskId);
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
            if (sub.getStartTime() != null) prioritizedTasks.remove(sub);
            historyManager.remove(sub.getId());
        }
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
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
        if (epic == null) return -1;
        if (subtask.getStartTime() != null && subtask.getEndTime() != null && taskOverlapsAny(subtask)) {
            return -1;
        }
        int id = nextId++;
        subtask.setId(id);
        subtasks.put(id, subtask);
        addToPrioritizedIfNeeded(subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;
        Subtask existing = subtasks.get(subtask.getId());
        if (existing.getStartTime() != null) prioritizedTasks.remove(existing);
        if (subtask.getStartTime() != null && subtask.getEndTime() != null && taskOverlapsAny(subtask)) {
            addToPrioritizedIfNeeded(existing);
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedIfNeeded(subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null) prioritizedTasks.remove(subtask);
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
        if (epic == null) return List.of();
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
     * Пересечение двух задач по времени (P1 < P4 && P2 > P3).
     */
    protected boolean tasksOverlap(Task a, Task b) {
        LocalDateTime startA = a.getStartTime(), endA = a.getEndTime();
        LocalDateTime startB = b.getStartTime(), endB = b.getEndTime();
        if (startA == null || endA == null || startB == null || endB == null) return false;
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    protected boolean taskOverlapsAny(Task task) {
        return prioritizedTasks.stream().anyMatch(o -> o.getId() != task.getId() && tasksOverlap(task, o));
    }

    /**
     * Для загрузки из файла: добавить задачу в приоритизированный список, если задан startTime.
     */
    protected void addToPrioritizedIfNeeded(Task task) {
        if (task.getStartTime() != null) prioritizedTasks.add(task);
    }

    protected void updateEpicStatus(int epicId) {
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
        long totalMinutes = 0;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        for (Subtask sub : subs) {
            if (sub.getDuration() != null) totalMinutes += sub.getDuration().toMinutes();
            LocalDateTime st = sub.getStartTime();
            if (st != null) minStart = minStart == null ? st : (st.isBefore(minStart) ? st : minStart);
            LocalDateTime et = sub.getEndTime();
            if (et != null) maxEnd = maxEnd == null ? et : (et.isAfter(maxEnd) ? et : maxEnd);
        }
        epic.setDuration(Duration.ofMinutes(totalMinutes));
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

    protected void restoreTask(Task task) {
        tasks.put(task.getId(), task);
    }

    protected void restoreEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void restoreSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }
    }

    protected void setNextId(int id) {
        nextId = id;
    }
}