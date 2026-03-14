package ru.practicum.kanban.http.handler;

import ru.practicum.kanban.http.ApiPath;

import java.util.regex.Pattern;

/**
 * Разбор пути запроса: нормализация, проверка «спискового» пути, извлечение id.
 * Учитывает полный путь (/tasks/1) и относительный (/1) для совместимости с разными контекстами.
 */
public final class PathMatcher {

    private static final Pattern NUMERIC_ID = Pattern.compile("\\d+");

    private PathMatcher() {
    }

    /**
     * Нормализованный путь: без завершающего слэша и лишних пробелов.
     */
    public static String normalize(String path) {
        if (path == null) {
            return "";
        }
        path = path.trim();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Путь соответствует запросу списка для данного базового пути.
     * Например: для TASKS - "", "/", "/tasks".
     */
    public static boolean isListPath(String path, String basePath) {
        if (path == null) {
            return true;
        }
        path = path.trim();
        return path.isEmpty()
                || "/".equals(path)
                || basePath.equals(path)
                || basePath.substring(1).equals(path);
    }

    /**
     * Извлекает числовой id из пути вида /basePath/id или /id или id.
     */
    public static Integer extractId(String path, String basePath) {
        if (path == null) {
            return null;
        }
        String withSlash = basePath + "/";
        if (path.startsWith(withSlash)) {
            String rest = path.substring(withSlash.length());
            return parsePositiveInt(rest);
        }
        if (path.startsWith("/") && path.length() > 1) {
            String rest = path.substring(1);
            return parsePositiveInt(rest);
        }
        return parsePositiveInt(path);
    }

    /**
     * Путь вида /epics/{id}/subtasks - подзадачи эпика.
     */
    public static boolean isEpicSubtasksPath(String path, String basePath) {
        return path != null
                && path.endsWith(ApiPath.EPIC_SUBTASKS_SUFFIX)
                && path.length() > ApiPath.EPIC_SUBTASKS_SUFFIX.length();
    }

    /**
     * Извлекает id эпика из пути вида /epics/{id}/subtasks или /{id}/subtasks.
     */
    public static Integer extractEpicIdFromSubtasksPath(String path, String basePath) {
        if (path == null || !path.endsWith(ApiPath.EPIC_SUBTASKS_SUFFIX)) {
            return null;
        }
        String prefix = path.substring(0, path.length() - ApiPath.EPIC_SUBTASKS_SUFFIX.length());
        return extractId(prefix, basePath);
    }

    private static Integer parsePositiveInt(String s) {
        if (s == null || !NUMERIC_ID.matcher(s).matches()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
