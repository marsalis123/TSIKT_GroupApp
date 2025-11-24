package com.example.test_1;

public class JobLog {
    public int id, jobId, userId;
    public String workText, commitMsg, pdfPath, createdAt, authorName;

    public JobLog(int id, int jobId, int userId, String workText, String commitMsg, String pdfPath, String createdAt, String authorName) {
        this.id = id; this.jobId = jobId; this.userId = userId;
        this.workText = workText; this.commitMsg = commitMsg;
        this.pdfPath = pdfPath; this.createdAt = createdAt; this.authorName = authorName;
    }
}
