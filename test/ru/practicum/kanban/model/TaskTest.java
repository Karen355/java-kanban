package ru.practicum.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldBeEqualWhenSameId() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", Status.DONE);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Task task1 = new Task("Task", "Description", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("Task", "Description", Status.NEW);
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разным ID не должны быть равны");
    }

    @Test
    void shouldHaveSameHashCodeWhenSameId() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", Status.DONE);
        task2.setId(1);

        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды задач с одинаковым ID должны совпадать");
    }

    @Test
    void subtaskShouldBeEqualWhenSameId() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, 2);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    void epicShouldBeEqualWhenSameId() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        epic1.setId(1);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }
}