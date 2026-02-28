package ru.practicum.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    /**
     * Время окончания эпика (расчётное: окончание самой поздней подзадачи).
     */
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    /**
     * Для эпика duration задаётся менеджером (сумма продолжительностей подзадач).
     */
    @Override
    public void setDuration(Duration duration) {
        super.setDuration(duration);
    }

    /**
     * Для эпика startTime задаётся менеджером (старт самой ранней подзадачи).
     */
    @Override
    public void setStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }


    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                ", endTime=" + endTime +
                '}';
    }
}
