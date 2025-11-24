package com.example.test_1;


public class FeedMessage {
    public int id;
    public int groupId;
    public String title;
    public String content;
    public String pdfPath;
    public int createdBy;
    public String createdAt; // alebo LocalDateTime

    public FeedMessage(int id, int groupId, String title, String content, String pdfPath, int createdBy, String createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.content = content;
        this.pdfPath = pdfPath;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}
