package ru.practicum.kanban.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldCreateManagerViaManagers() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
    }
}
