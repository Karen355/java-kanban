public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаем задачи
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпики с подзадачами
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

        // Печатаем списки
        System.out.println("=== Все задачи ===");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\n=== Все эпики ===");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\n=== Все подзадачи ===");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Изменяем статусы
        System.out.println("\n=== После изменения статусов ===");
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        // Печатаем обновленные списки
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Удаляем одну задачу и один эпик
        System.out.println("\n=== После удаления ===");
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println("Оставшиеся задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Оставшиеся эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("Оставшиеся подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}