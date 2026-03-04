package ru.practicum.kanban.manager;

/**
 * Непроверяемое исключение при ошибке сохранения/загрузки менеджера в файл.
 */
public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
