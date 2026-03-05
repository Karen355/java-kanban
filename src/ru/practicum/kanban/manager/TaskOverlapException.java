package ru.practicum.kanban.manager;

/**
 * Исключение при создании или обновлении задачи, пересекающейся по времени с существующими.
 */
public class TaskOverlapException extends RuntimeException {

    public TaskOverlapException(String message) {
        super(message);
    }
}
