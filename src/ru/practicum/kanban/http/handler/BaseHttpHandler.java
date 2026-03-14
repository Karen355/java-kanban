package ru.practicum.kanban.http.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Базовый обработчик: чтение тела запроса и отправка ответов по ТЗ
 * (200, 201, 404, 406, 500) с JSON и единым Content-Type.
 */
public class BaseHttpHandler {

    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    private static final String MESSAGE_NOT_FOUND = "Ресурс не найден";
    private static final String MESSAGE_OVERLAP = "Задача пересекается по времени с существующими";
    private static final String MESSAGE_INTERNAL_ERROR = "Внутренняя ошибка сервера";

    protected static final int SC_OK = 200;
    protected static final int SC_CREATED = 201;
    protected static final int SC_NOT_FOUND = 404;
    protected static final int SC_NOT_ACCEPTABLE = 406;
    protected static final int SC_INTERNAL_ERROR = 500;

    protected String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, SC_OK);
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, SC_CREATED);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendWithMessage(exchange, MESSAGE_NOT_FOUND, SC_NOT_FOUND);
    }

    /**
     * Для использования из диспетчера при неизвестном пути.
     */
    protected static void sendNotFoundStatic(HttpExchange exchange) throws IOException {
        byte[] bytes = MESSAGE_NOT_FOUND.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        exchange.sendResponseHeaders(SC_NOT_FOUND, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendWithMessage(exchange, MESSAGE_OVERLAP, SC_NOT_ACCEPTABLE);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendWithMessage(exchange, MESSAGE_INTERNAL_ERROR, SC_INTERNAL_ERROR);
    }

    private static void sendWithMessage(HttpExchange exchange, String message, int statusCode) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
