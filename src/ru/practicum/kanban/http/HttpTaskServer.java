package ru.practicum.kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.kanban.http.handler.*;
import ru.practicum.kanban.manager.Managers;
import ru.practicum.kanban.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * HTTP-сервер для доступа к TaskManager через REST API (порт 8080).
 */
public class HttpTaskServer {

    private static final int PORT = 8080;

    private final TaskManager manager;
    private final Gson gson;
    private HttpServer server;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        this.gson = GsonFactory.createGson();
    }

    /**
     * Возвращает экземпляр Gson для тестов (сериализация/десериализация).
     */
    public static Gson getGson() {
        return GsonFactory.createGson();
    }

    public void start() throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootHandler(manager, gson));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
        System.out.println("HTTP Task Server запущен на порту " + PORT);
    }
}
