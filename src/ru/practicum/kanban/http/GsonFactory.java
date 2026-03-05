package ru.practicum.kanban.http;

import com.google.gson.*;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Status;
import ru.practicum.kanban.model.Subtask;
import ru.practicum.kanban.model.Task;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Фабрика Gson: сериализация типов java.time (LocalDateTime, Duration) и моделей Task, Subtask, Epic.
 */
public final class GsonFactory {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private GsonFactory() {
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Status.class, new StatusAdapter())
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
    }

    private static String getString(JsonObject o, String key) {
        if (o == null || !o.has(key)) return null;
        JsonElement e = o.get(key);
        return e.isJsonNull() ? null : e.getAsString();
    }

    private static class TaskAdapter implements JsonDeserializer<Task>, JsonSerializer<Task> {
        @Override
        public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            String title = getString(o, "title");
            String description = getString(o, "description");
            String statusStr = getString(o, "status");
            if (title == null || description == null || statusStr == null) {
                throw new JsonParseException("title, description и status обязательны");
            }
            Task task = new Task(title, description, Status.valueOf(statusStr));
            if (o.has("id") && !o.get("id").isJsonNull()) {
                task.setId(o.get("id").getAsInt());
            }
            if (o.has("duration") && !o.get("duration").isJsonNull()) {
                task.setDuration(Duration.ofMinutes(o.get("duration").getAsLong()));
            }
            if (o.has("startTime") && !o.get("startTime").isJsonNull()) {
                task.setStartTime(LocalDateTime.parse(o.get("startTime").getAsString(), DATE_TIME_FORMATTER));
            }
            return task;
        }

        @Override
        public JsonElement serialize(Task src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject o = new JsonObject();
            o.addProperty("id", src.getId());
            o.addProperty("title", src.getTitle());
            o.addProperty("description", src.getDescription());
            if (src.getStatus() != null) {
                o.addProperty("status", src.getStatus().name());
            }
            if (src.getDuration() != null) {
                o.addProperty("duration", src.getDuration().toMinutes());
            }
            if (src.getStartTime() != null) {
                o.addProperty("startTime", src.getStartTime().format(DATE_TIME_FORMATTER));
            }
            return o;
        }
    }

    private static class SubtaskAdapter implements JsonDeserializer<Subtask>, JsonSerializer<Subtask> {
        @Override
        public Subtask deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            if (!o.has("epicId") || o.get("epicId").isJsonNull()) {
                throw new JsonParseException("epicId обязателен для подзадачи");
            }
            int epicId = o.get("epicId").getAsInt();
            String title = getString(o, "title");
            String description = getString(o, "description");
            String statusStr = getString(o, "status");
            if (title == null || description == null || statusStr == null) {
                throw new JsonParseException("title, description и status обязательны");
            }
            Subtask subtask = new Subtask(title, description, Status.valueOf(statusStr), epicId);
            if (o.has("id") && !o.get("id").isJsonNull()) {
                subtask.setId(o.get("id").getAsInt());
            }
            if (o.has("duration") && !o.get("duration").isJsonNull()) {
                subtask.setDuration(Duration.ofMinutes(o.get("duration").getAsLong()));
            }
            if (o.has("startTime") && !o.get("startTime").isJsonNull()) {
                subtask.setStartTime(LocalDateTime.parse(o.get("startTime").getAsString(), DATE_TIME_FORMATTER));
            }
            return subtask;
        }

        @Override
        public JsonElement serialize(Subtask src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject o = (JsonObject) new TaskAdapter().serialize(src, typeOfSrc, context);
            o.addProperty("epicId", src.getEpicId());
            return o;
        }
    }

    private static class EpicAdapter implements JsonDeserializer<Epic>, JsonSerializer<Epic> {
        @Override
        public Epic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            String title = getString(o, "title");
            String description = getString(o, "description");
            if (title == null || description == null) {
                throw new JsonParseException("title и description обязательны для эпика");
            }
            Epic epic = new Epic(title, description);
            if (o.has("id") && !o.get("id").isJsonNull()) {
                epic.setId(o.get("id").getAsInt());
            }
            if (o.has("status") && !o.get("status").isJsonNull()) {
                epic.setStatus(Status.valueOf(o.get("status").getAsString()));
            }
            return epic;
        }

        @Override
        public JsonElement serialize(Epic src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject o = new JsonObject();
            o.addProperty("id", src.getId());
            o.addProperty("title", src.getTitle());
            o.addProperty("description", src.getDescription());
            o.addProperty("status", src.getStatus() != null ? src.getStatus().name() : null);
            o.add("subtaskIds", context.serialize(src.getSubtaskIds()));
            return o;
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(DATE_TIME_FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull() || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(json.getAsString(), DATE_TIME_FORMATTER);
        }
    }

    private static class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
        @Override
        public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toMinutes());
        }

        @Override
        public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) {
                return null;
            }
            return Duration.ofMinutes(json.getAsLong());
        }
    }

    private static class StatusAdapter implements JsonSerializer<Status>, JsonDeserializer<Status> {
        @Override
        public JsonElement serialize(Status src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.name());
        }

        @Override
        public Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull() || json.getAsString().isEmpty()) {
                return null;
            }
            return Status.valueOf(json.getAsString());
        }
    }
}
