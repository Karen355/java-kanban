package ru.practicum.kanban;

import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.manager.Managers;
import ru.practicum.kanban.model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        int task1Id = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        int task2Id = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпики и подзадачи
        int epic1Id = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        int subtask1Id = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1Id));
        int subtask2Id = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1Id));

        int epic2Id = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        int subtask3Id = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2Id));

        // Просматриваем задачи для формирования истории
        System.out.println("=== Формируем историю просмотров ===");
        manager.getTaskById(task1Id);
        manager.getEpicById(epic1Id);
        manager.getSubtaskById(subtask1Id);
        manager.getTaskById(task2Id);
        manager.getSubtaskById(subtask3Id);

        // Печатаем списки
        printAllTasks(manager);

        // Изменяем статусы
        System.out.println("\n=== После изменения статусов ===");
        Task task1 = manager.getTaskById(task1Id);
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        Subtask subtask1 = manager.getSubtaskById(subtask1Id);
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        // Просматриваем еще раз для истории
        manager.getSubtaskById(subtask2Id);
        manager.getEpicById(epic2Id);

        // Выводим обновленные списки
        printAllTasks(manager);

        // Удаляем одну задачу и один эпик
        System.out.println("\n=== После удаления ===");
        manager.deleteTaskById(task1Id);
        manager.deleteEpicById(epic1Id);

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        System.out.println("История просмотров (" + manager.getHistory().size() + "):");
        manager.getHistory().forEach(task ->
                System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ")")
        );
    }
}