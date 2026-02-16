package ru.practicum.kanban.manager;

import ru.practicum.kanban.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * История просмотров на основе двусвязного списка и HashMap.
 * Добавление и удаление по id выполняются за O(1): узел находится по id в map,
 * удаление узла по ссылке в списке - константное время.
 */
public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodeByTaskId;
    private Node head;
    private Node tail;

    public InMemoryHistoryManager() {
        nodeByTaskId = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        Node existingNode = nodeByTaskId.get(id);
        if (existingNode != null) {
            removeNode(existingNode);
        }
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = nodeByTaskId.get(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        nodeByTaskId.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }

        nodeByTaskId.remove(node.task.getId());
    }

    /**
     * Узел двусвязного списка для хранения задачи в истории.
     */
    private static class Node {
        final Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}
