package ru.practicum.kanban.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.http.ApiPath;
import ru.practicum.kanban.http.HttpMethod;
import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = PathMatcher.normalize(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod();

            if (HttpMethod.GET.equals(method) && PathMatcher.isListPath(path, ApiPath.PRIORITIZED)) {
                List<Task> prioritized = manager.getPrioritizedTasks();
                sendText(exchange, gson.toJson(prioritized));
                return;
            }

            sendNotFound(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
