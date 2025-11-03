package ru.practicum.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithCorrectInitialState() {
        Subtask subtask = new Subtask("Test Subtask", "Test Description", Status.NEW, 1);

        assertEquals("Test Subtask", subtask.getTitle());
        assertEquals("Test Description", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(1, subtask.getEpicId());
    }

    @Test
    void shouldGetEpicId() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.IN_PROGRESS, 42);

        assertEquals(42, subtask.getEpicId());
    }

    @Test
    void shouldInheritTaskPropertiesCorrectly() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 1);
        subtask.setId(5);
        subtask.setTitle("Updated Title");
        subtask.setDescription("Updated Description");
        subtask.setStatus(Status.DONE);

        assertEquals(5, subtask.getId());
        assertEquals("Updated Title", subtask.getTitle());
        assertEquals("Updated Description", subtask.getDescription());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals(1, subtask.getEpicId()); // epicId не должен меняться
    }

    @Test
    void shouldHaveCorrectToString() {
        Subtask subtask = new Subtask("Test Subtask", "Test Description", Status.IN_PROGRESS, 10);
        subtask.setId(3);

        String toString = subtask.toString();

        assertTrue(toString.contains("Subtask{"));
        assertTrue(toString.contains("id=3"));
        assertTrue(toString.contains("title='Test Subtask'"));
        assertTrue(toString.contains("description='Test Description'"));
        assertTrue(toString.contains("status=IN_PROGRESS"));
        assertTrue(toString.contains("epicId=10"));
    }

    @Test
    void shouldBeEqualWhenSameId() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, 2);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Subtask subtask1 = new Subtask("Subtask", "Description", Status.NEW, 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask", "Description", Status.NEW, 1);
        subtask2.setId(2);

        assertNotEquals(subtask1, subtask2, "Подзадачи с разным ID не должны быть равны");
    }

    @Test
    void shouldHaveSameHashCodeWhenSameId() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, 2);
        subtask2.setId(1);

        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хэш-коды подзадач с одинаковым ID должны совпадать");
    }

    @Test
    void shouldCreateSubtaskWithDifferentStatuses() {
        Subtask subtaskNew = new Subtask("Subtask", "Description", Status.NEW, 1);
        Subtask subtaskInProgress = new Subtask("Subtask", "Description", Status.IN_PROGRESS, 1);
        Subtask subtaskDone = new Subtask("Subtask", "Description", Status.DONE, 1);

        assertEquals(Status.NEW, subtaskNew.getStatus());
        assertEquals(Status.IN_PROGRESS, subtaskInProgress.getStatus());
        assertEquals(Status.DONE, subtaskDone.getStatus());
    }

    @Test
    void shouldCreateSubtaskWithDifferentEpicIds() {
        Subtask subtask1 = new Subtask("Subtask", "Description", Status.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask", "Description", Status.NEW, 999);
        Subtask subtask3 = new Subtask("Subtask", "Description", Status.NEW, 0);

        assertEquals(1, subtask1.getEpicId());
        assertEquals(999, subtask2.getEpicId());
        assertEquals(0, subtask3.getEpicId());
    }

    @Test
    void epicIdShouldBeFinalAndNotChangeable() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 5);

        // epicId - final поле, его нельзя изменить после создания
        // Это проверяется компилятором, но мы можем убедиться, что геттер возвращает правильное значение
        assertEquals(5, subtask.getEpicId());

        // Даже после изменения других свойств, epicId остается тем же
        subtask.setId(10);
        subtask.setTitle("New Title");
        subtask.setStatus(Status.DONE);

        assertEquals(5, subtask.getEpicId());
    }
}