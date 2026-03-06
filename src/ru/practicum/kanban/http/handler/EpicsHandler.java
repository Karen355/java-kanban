package ru.practicum.kanban.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.http.ApiPath;
import ru.practicum.kanban.http.HttpMethod;
import ru.practicum.kanban.exception.NotFoundException;
import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = PathMatcher.normalize(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod();

            if (HttpMethod.GET.equals(method) && PathMatcher.isListPath(path, ApiPath.EPICS)) {
                sendText(exchange, gson.toJson(manager.getAllEpics()));
                return;
            }

            if (HttpMethod.GET.equals(method) && PathMatcher.isEpicSubtasksPath(path, ApiPath.EPICS)) {
                Integer epicId = PathMatcher.extractEpicIdFromSubtasksPath(path, ApiPath.EPICS);
                if (epicId != null) {
                    List<Subtask> subtasks = manager.getSubtasksByEpicId(epicId);
                    sendText(exchange, gson.toJson(subtasks));
                    return;
                }
            }

            if (HttpMethod.GET.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.EPICS);
                if (id != null) {
                    Epic epic = manager.getEpicById(id);
                    sendText(exchange, gson.toJson(epic));
                    return;
                }
            }

            if (HttpMethod.POST.equals(method) && PathMatcher.isListPath(path, ApiPath.EPICS)) {
                Epic epic = gson.fromJson(readBody(exchange), Epic.class);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }
                if (epic.getId() == 0) {
                    manager.createEpic(epic);
                    sendCreated(exchange, gson.toJson(epic));
                } else {
                    manager.updateEpic(epic);
                    sendText(exchange, gson.toJson(epic));
                }
                return;
            }

            if (HttpMethod.DELETE.equals(method)) {
                Integer id = PathMatcher.extractId(path, ApiPath.EPICS);
                if (id != null) {
                    manager.deleteEpicById(id);
                    sendText(exchange, "");
                    return;
                }
            }

            sendNotFound(exchange);
        } catch (NotFoundException | JsonParseException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
