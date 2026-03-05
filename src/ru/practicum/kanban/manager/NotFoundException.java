package ru.practicum.kanban.manager;

/**
 * Исключение при обращении к несуществующему ресурсу (задача, подзадача, эпик).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
