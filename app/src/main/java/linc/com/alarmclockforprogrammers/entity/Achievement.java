package linc.com.alarmclockforprogrammers.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ColumnInfo.INTEGER;
import static android.arch.persistence.room.ColumnInfo.TEXT;

@Entity(tableName = "achievements")
public class Achievement {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "award", typeAffinity = INTEGER)
    private int award;

    @ColumnInfo(name = "tasks_to_complete", typeAffinity = INTEGER)
    private int tasksToComplete;

    @Ignore
    private int completedTasks;

    @ColumnInfo(name = "achievement_task", typeAffinity = TEXT)
    private String achievementTask;

    @ColumnInfo(name = "language", typeAffinity = TEXT)
    private String language;

    @ColumnInfo(name = "completed", typeAffinity = INTEGER)
    private boolean completed;


    public Achievement(int id, int award, int tasksToComplete, String achievementTask, String language, boolean completed) {
        this.id = id;
        this.award = award;
        this.tasksToComplete = tasksToComplete;
        this.achievementTask = achievementTask;
        this.language = language;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getTasksToComplete() {
        return tasksToComplete;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public void setTasksToComplete(int tasksToComplete) {
        this.tasksToComplete = tasksToComplete;
    }

    public String getAchievementTask() {
        return achievementTask;
    }

    public void setAchievementTask(String achievementTask) {
        this.achievementTask = achievementTask;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}