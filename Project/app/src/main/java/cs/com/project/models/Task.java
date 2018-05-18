package cs.com.project.models;

import cs.com.project.FirebaseConstants;

public class Task {
    public String title;
    public String ownerId;
    public String ownerName;
    public boolean isCompleted;



    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    public Task(String title) {
        this.title = title;
        this.isCompleted = false;
        this.ownerName = FirebaseConstants.UNCLAIMED_TASK;
    }
}
