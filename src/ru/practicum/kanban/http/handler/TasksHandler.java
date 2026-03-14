package ru.practicum.kanban.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.http.ApiPath;
import ru.practicum.kanban.http.HttpMethod;
import ru.practicum.kanban.exception.NotFoundException;
import ru.practicum.kanban.exception.TaskOverlapException;
import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = PathMatcher.normalize(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod();

            if (HttpMethod.GET.equals(method) && PathMatcher.isListPath(path, ApiPath.TASKS)) {
                sendText(exchange, gson.toJson(manager.getAllTasks()));
                return;
            }

            if (HttpMethod.GET.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.TASKS);
                if (id != null) {
                    Task task = manager.getTaskById(id);
                    sendText(exchange, gson.toJson(task));
                    return;
                }
            }

            if (HttpMethod.POST.equals(method) && PathMatcher.isListPath(path, ApiPath.TASKS)) {
                Task task = gson.fromJson(readBody(exchange), Task.class);
                if (task == null) {
                    sendNotFound(exchange);
                    return;
                }
                if (task.getId() == 0) {
                    manager.createTask(task);
                    sendCreated(exchange, gson.toJson(task));
                } else {
                    manager.updateTask(task);
                    sendText(exchange, gson.toJson(task));
                }
                return;
            }

            if (HttpMethod.DELETE.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.TASKS);
                if (id != null) {
                    manager.deleteTaskById(id);
                    sendText(exchange, "");
                    return;
                }
            }

            sendNotFound(exchange);
        } catch (NotFoundException | JsonParseException e) {
            sendNotFound(exchange);
        } catch (TaskOverlapException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
