package ru.practicum.kanban;

import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаем задачи
        int task1Id = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        int task2Id = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпики и подзадачи
        int epic1Id = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        int subtask1Id = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1Id));
        int subtask2Id = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1Id));

        int epic2Id = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        int subtask3Id = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2Id));

        // Печатаем списки
        System.out.println("=== Все задачи ===");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\n=== Все эпики ===");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("\n=== Все подзадачи ===");
        manager.getAllSubtasks().forEach(System.out::println);

        // Изменяем статусы
        System.out.println("\n=== После изменения статусов ===");
        Task task1 = manager.getTaskById(task1Id);
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        Subtask subtask1 = manager.getSubtaskById(subtask1Id);
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        Subtask subtask2 = manager.getSubtaskById(subtask2Id);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        Subtask subtask3 = manager.getSubtaskById(subtask3Id);
        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        // Выводим обновленные списки
        System.out.println("Задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        // Удаляем одну задачу и один эпик
        System.out.println("\n=== После удаления ===");
        manager.deleteTaskById(task1Id);
        manager.deleteEpicById(epic1Id);

        System.out.println("Оставшиеся задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Оставшиеся эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Оставшиеся подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);
    }
}
