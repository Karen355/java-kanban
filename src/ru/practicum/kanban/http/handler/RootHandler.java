package ru.practicum.kanban.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.http.ApiPath;
import ru.practicum.kanban.manager.TaskManager;

import java.io.IOException;

/**
 * Единая точка входа: диспетчеризация по префиксу пути из ApiPath.
 */
public class RootHandler implements HttpHandler {

    private final TasksHandler tasksHandler;
    private final SubtasksHandler subtasksHandler;
    private final EpicsHandler epicsHandler;
    private final HistoryHandler historyHandler;
    private final PrioritizedHandler prioritizedHandler;

    public RootHandler(TaskManager manager, Gson gson) {
        this.tasksHandler = new TasksHandler(manager, gson);
        this.subtasksHandler = new SubtasksHandler(manager, gson);
        this.epicsHandler = new EpicsHandler(manager, gson);
        this.historyHandler = new HistoryHandler(manager, gson);
        this.prioritizedHandler = new PrioritizedHandler(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null) {
            path = "";
        }

        HttpHandler handler = handlerForPath(path);
        if (handler != null) {
            handler.handle(exchange);
        } else {
            BaseHttpHandler.sendNotFoundStatic(exchange);
        }
    }

    private HttpHandler handlerForPath(String path) {
        if (path.startsWith(ApiPath.TASKS)) {
            return tasksHandler;
        }
        if (path.startsWith(ApiPath.SUBTASKS)) {
            return subtasksHandler;
        }
        if (path.startsWith(ApiPath.EPICS)) {
            return epicsHandler;
        }
        if (path.startsWith(ApiPath.HISTORY)) {
            return historyHandler;
        }
        if (path.startsWith(ApiPath.PRIORITIZED)) {
            return prioritizedHandler;
        }
        return null;
    }
}
