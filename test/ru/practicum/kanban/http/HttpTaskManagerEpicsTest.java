package ru.practicum.kanban.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.manager.InMemoryTaskManager;
import ru.practicum.kanban.manager.TaskManager;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest {

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
    void getEpicsEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    void addEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic title", "Epic desc");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> list = manager.getAllEpics();
        assertEquals(1, list.size());
        assertEquals("Epic title", list.get(0).getTitle());
    }

    @Test
    void getEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("E", "D");
        int id = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic parsed = gson.fromJson(response.body(), Epic.class);
        assertEquals(id, parsed.getId());
        assertEquals("E", parsed.getTitle());
    }

    @Test
    void getEpicByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/99999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("E", "D");
        int epicId = manager.createEpic(epic);
        manager.createSubtask(new Subtask("S1", "D", ru.practicum.kanban.model.Status.NEW, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("S1"));
    }

    @Test
    void getEpicSubtasksEpicNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/99999/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("E", "D");
        int id = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(Exception.class, () -> manager.getEpicById(id));
    }
}
