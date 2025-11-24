package com.example.test_1;


public class Job {
    public int id;
    public String title, description;
    public int groupId;
    public String status;
    public int createdBy;
    public int assignedTo;
    public String createdAt;

    public Job(int id, String title, String desc, int groupId, String status, int createdBy, int assignedTo, String createdAt) {
        this.id = id; this.title = title; this.description = desc; this.groupId = groupId;
        this.status = status; this.createdBy = createdBy; this.assignedTo = assignedTo; this.createdAt = createdAt;
    }
}
