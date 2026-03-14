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
import ru.practicum.kanban.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = PathMatcher.normalize(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod();

            if (HttpMethod.GET.equals(method) && PathMatcher.isListPath(path, ApiPath.SUBTASKS)) {
                sendText(exchange, gson.toJson(manager.getAllSubtasks()));
                return;
            }

            if (HttpMethod.GET.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.SUBTASKS);
                if (id != null) {
                    Subtask subtask = manager.getSubtaskById(id);
                    sendText(exchange, gson.toJson(subtask));
                    return;
                }
            }

            if (HttpMethod.POST.equals(method) && PathMatcher.isListPath(path, ApiPath.SUBTASKS)) {
                Subtask subtask = gson.fromJson(readBody(exchange), Subtask.class);
                if (subtask == null) {
                    sendNotFound(exchange);
                    return;
                }
                if (subtask.getId() == 0) {
                    manager.createSubtask(subtask);
                    sendCreated(exchange, gson.toJson(subtask));
                } else {
                    manager.updateSubtask(subtask);
                    sendText(exchange, gson.toJson(subtask));
                }
                return;
            }

            if (HttpMethod.DELETE.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.SUBTASKS);
                if (id != null) {
                    manager.deleteSubtaskById(id);
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
