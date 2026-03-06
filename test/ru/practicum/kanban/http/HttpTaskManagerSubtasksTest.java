package ru.practicum.kanban.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.manager.InMemoryTaskManager;
import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void getSubtasksEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    void addSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epicId);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> list = manager.getAllSubtasks();
        assertEquals(1, list.size());
        assertEquals("Sub", list.get(0).getTitle());
        assertEquals(epicId, list.get(0).getEpicId());
    }

    @Test
    void getSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("E", "D");
        int epicId = manager.createEpic(epic);
        Subtask sub = new Subtask("S", "D", Status.NEW, epicId);
        int id = manager.createSubtask(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask parsed = gson.fromJson(response.body(), Subtask.class);
        assertEquals(id, parsed.getId());
        assertEquals("S", parsed.getTitle());
    }

    @Test
    void getSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/99999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("E", "D");
        int epicId = manager.createEpic(epic);
        Subtask sub = new Subtask("S", "D", Status.NEW, epicId);
        int id = manager.createSubtask(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(Exception.class, () -> manager.getSubtaskById(id));
    }
}
