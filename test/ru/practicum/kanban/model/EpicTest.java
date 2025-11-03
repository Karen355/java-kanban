package ru.practicum.kanban.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void shouldCreateEpicWithCorrectInitialState() {
        Epic epic = new Epic("Test Epic", "Test Description");

        assertEquals("Test Epic", epic.getTitle());
        assertEquals("Test Description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldAddSubtaskId() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(3, subtaskIds.size());
        assertTrue(subtaskIds.contains(1));
        assertTrue(subtaskIds.contains(2));
        assertTrue(subtaskIds.contains(3));
    }

    @Test
    void shouldRemoveSubtaskId() {
        Epic epic = new Epic("Epic", "Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        epic.removeSubtaskId(2);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(1));
        assertFalse(subtaskIds.contains(2));
        assertTrue(subtaskIds.contains(3));
    }

    @Test
    void shouldRemoveSubtaskIdWhenMultipleSameIds() {
        Epic epic = new Epic("Epic", "Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(1); // дублирование
        epic.addSubtaskId(2);

        epic.removeSubtaskId(1);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());
        assertEquals(1, (int) subtaskIds.get(0));
        assertEquals(2, (int) subtaskIds.get(1));
    }

    @Test
    void shouldHandleRemovingNonExistentSubtaskId() {
        Epic epic = new Epic("Epic", "Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        // Пытаемся удалить несуществующий ID
        epic.removeSubtaskId(999);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(1));
        assertTrue(subtaskIds.contains(2));
    }

    @Test
    void shouldHaveCorrectToString() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.setId(1);
        epic.addSubtaskId(10);
        epic.addSubtaskId(20);

        String toString = epic.toString();

        assertTrue(toString.contains("Epic{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("title='Test Epic'"));
        assertTrue(toString.contains("description='Test Description'"));
        assertTrue(toString.contains("status=NEW"));
        assertTrue(toString.contains("subtaskIds=[10, 20]") || toString.contains("subtaskIds=[20, 10]"));
    }

    @Test
    void shouldInheritTaskPropertiesCorrectly() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(5);
        epic.setTitle("Updated Title");
        epic.setDescription("Updated Description");
        epic.setStatus(Status.IN_PROGRESS);

        assertEquals(5, epic.getId());
        assertEquals("Updated Title", epic.getTitle());
        assertEquals("Updated Description", epic.getDescription());
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldMaintainSubtaskIdsOrder() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(3);
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(3, subtaskIds.size());
        assertEquals(3, (int) subtaskIds.get(0));
        assertEquals(1, (int) subtaskIds.get(1));
        assertEquals(2, (int) subtaskIds.get(2));
    }
}