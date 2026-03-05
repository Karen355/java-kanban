package ru.practicum.kanban.http;

/**
 * Константы путей API по ТЗ: /tasks, /subtasks, /epics, /history, /prioritized.
 */
public final class ApiPath {

    public static final String TASKS = "/tasks";
    public static final String SUBTASKS = "/subtasks";
    public static final String EPICS = "/epics";
    public static final String HISTORY = "/history";
    public static final String PRIORITIZED = "/prioritized";

    /**
     * Суффикс для подзадач эпика: GET /epics/{id}/subtasks
     */
    public static final String EPIC_SUBTASKS_SUFFIX = "/subtasks";

    private ApiPath() {
    }
}
