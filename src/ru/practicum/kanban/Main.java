package ru.practicum.kanban;

import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.manager.Managers;
import ru.practicum.kanban.model.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаём две задачи, эпик с тремя подзадачами и эпик без подзадач
        int task1Id = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        int task2Id = manager.createTask(new Task("Задача 2", "Описание 2", Status.NEW));

        int epicWithSubtasksId = manager.createEpic(new Epic("Эпик с подзадачами", "Описание эпика"));
        int sub1Id = manager.createSubtask(new Subtask("Подзадача 1", "Описание", Status.NEW, epicWithSubtasksId));
        int sub2Id = manager.createSubtask(new Subtask("Подзадача 2", "Описание", Status.NEW, epicWithSubtasksId));
        int sub3Id = manager.createSubtask(new Subtask("Подзадача 3", "Описание", Status.NEW, epicWithSubtasksId));

        int epicEmptyId = manager.createEpic(new Epic("Эпик без подзадач", "Пустой эпик"));

        // Запрашиваем задачи несколько раз в разном порядке
        System.out.println("=== Запросы в разном порядке ===\n");

        manager.getTaskById(task1Id);
        printHistoryAndCheckNoDuplicates(manager);

        manager.getEpicById(epicWithSubtasksId);
        printHistoryAndCheckNoDuplicates(manager);

        manager.getSubtaskById(sub2Id);
        manager.getSubtaskById(sub1Id);
        printHistoryAndCheckNoDuplicates(manager);

        manager.getTaskById(task2Id);
        manager.getTaskById(task1Id); // повторный просмотр - должен переместиться в конец
        printHistoryAndCheckNoDuplicates(manager);

        manager.getEpicById(epicEmptyId);
        manager.getSubtaskById(sub3Id);
        printHistoryAndCheckNoDuplicates(manager);

        // Удаляем задачу, которая есть в истории
        System.out.println("=== Удаляем задачу (ID: " + task2Id + ") из истории ===\n");
        manager.deleteTaskById(task2Id);
        List<Task> historyAfterDelete = manager.getHistory();
        boolean task2InHistory = historyAfterDelete.stream().anyMatch(t -> t.getId() == task2Id);
        System.out.println("История после удаления задачи " + task2Id + ":");
        printHistory(manager);
        System.out.println("Задача " + task2Id + " в истории: " + task2InHistory + " (ожидается false)\n");

        // Удаляем эпик с тремя подзадачами
        System.out.println("=== Удаляем эпик с подзадачами (ID: " + epicWithSubtasksId + ") ===\n");
        manager.deleteEpicById(epicWithSubtasksId);
        List<Task> historyAfterEpicDelete = manager.getHistory();
        boolean epicInHistory = historyAfterEpicDelete.stream().anyMatch(t -> t.getId() == epicWithSubtasksId);
        boolean sub1InHistory = historyAfterEpicDelete.stream().anyMatch(t -> t.getId() == sub1Id);
        boolean sub2InHistory = historyAfterEpicDelete.stream().anyMatch(t -> t.getId() == sub2Id);
        boolean sub3InHistory = historyAfterEpicDelete.stream().anyMatch(t -> t.getId() == sub3Id);
        System.out.println("История после удаления эпика:");
        printHistory(manager);
        System.out.println("Эпик " + epicWithSubtasksId + " в истории: " + epicInHistory + " (ожидается false)");
        System.out.println("Подзадачи " + sub1Id + ", " + sub2Id + ", " + sub3Id + " в истории: "
                + (sub1InHistory || sub2InHistory || sub3InHistory) + " (ожидается false)");
    }

    private static void printHistoryAndCheckNoDuplicates(TaskManager manager) {
        List<Task> history = manager.getHistory();
        Set<Integer> ids = history.stream().map(Task::getId).collect(Collectors.toSet());
        boolean noDuplicates = ids.size() == history.size();
        System.out.println("История (размер " + history.size() + ", без повторов: " + noDuplicates + "):");
        printHistory(manager);
        System.out.println();
    }

    private static void printHistory(TaskManager manager) {
        manager.getHistory().forEach(task ->
                System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ")")
        );
    }
}
