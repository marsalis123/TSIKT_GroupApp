package com.example.test_1;

import java.sql.*;
import java.util.*;

public class UserManager {

    public UserManager() {}

    //Update na server
    public boolean register(String username, String password) {
        try {
            String jsonReq = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/users/register"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //Update na server
    public User login(String username, String password) {
        try {
            String jsonReq = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/users/login"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return null;

            String body = resp.body();
            int id = extractInt(body, "\"id\":");
            String uname = extractString(body, "\"username\":\"");
            String name = extractString(body, "\"name\":\"");
            String email = extractString(body, "\"email\":\"");
            int age = extractInt(body, "\"age\":");
            String photo = extractString(body, "\"photoPath\":\""); // podľa UserDto

            User user = new User(uname, password);
            user.setId(id);
            user.setName(name);
            user.setEmail(email);
            user.setAge(age);
            user.setPhotoPath(photo);
            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean saveUser(User user) {
        try {
            String safeName = user.getName() == null ? "" : user.getName().replace("\"", "\\\"");
            String safeEmail = user.getEmail() == null ? "" : user.getEmail().replace("\"", "\\\"");
            String safePhoto = user.getPhotoPath() == null ? "" :
                    user.getPhotoPath().replace("\\", "\\\\").replace("\"", "\\\"");

            String jsonReq = String.format(
                    "{\"id\":%d,\"username\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"age\":%d,\"photoPath\":\"%s\"}",
                    user.getId(),
                    user.getUsername().replace("\"", "\\\""),
                    safeName,
                    safeEmail,
                    user.getAge(),
                    safePhoto
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/users/save"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //Skupiny - prechod na server
    public List<Group> getUserGroups(String username) {
        List<Group> groups = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/groups?username=" +
                    java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return groups;

            String body = resp.body();

            // veľmi jednoduchý parser bez knižnice – očakáva:
            // [ {"id":1,"name":"...","owner":"..."}, {...} ]
            String json = body.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return groups;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                String name = extractString(obj, "\"name\":\"");
                String owner = extractString(obj, "\"owner\":\"");

                groups.add(new Group(id, name, owner));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }


    public boolean addGroup(Group group) {
        try {
            String jsonReq = String.format(
                    "{\"id\":0,\"name\":\"%s\",\"owner\":\"%s\"}",
                    group.name.replace("\"", "\\\""),
                    group.owner.replace("\"", "\\\"")
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/groups"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteGroup(int groupId) {
        try {
            String url = "http://localhost:8080/api/groups/" + groupId;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .DELETE()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public boolean addUserToGroup(int groupId, int userId) {
        try {
            String jsonReq = String.format(
                    "{\"groupId\":%d,\"userId\":%d}",
                    groupId, userId
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/groups/members"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeUserFromGroup(int groupId, int userId) {
        try {
            String jsonReq = String.format(
                    "{\"groupId\":%d,\"userId\":%d}",
                    groupId, userId
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/groups/members"))
                    .header("Content-Type", "application/json")
                    .method("DELETE", java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/groups/" + groupId + "/members";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return members;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return members;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                String username = extractString(obj, "\"username\":\"");
                String name = extractString(obj, "\"name\":\"");
                String email = extractString(obj, "\"email\":\"");
                int age = extractInt(obj, "\"age\":");
                String photo = extractString(obj, "\"photoPath\":\"");

                User u = new User(username, "");
                u.setId(id);
                u.setName(name);
                u.setEmail(email);
                u.setAge(age);
                u.setPhotoPath(photo);
                members.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return members;
    }


    public User findUser(String value) {
        try {
            String url = "http://localhost:8080/api/users/find?value=" +
                    java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return null;

            String body = resp.body(); // {"id":...,"username":"...","name":"...","email":"...","age":...,"photoPath":"..."}

            int id = extractInt(body, "\"id\":");
            String username = extractString(body, "\"username\":\"");
            String name = extractString(body, "\"name\":\"");
            String email = extractString(body, "\"email\":\"");
            int age = extractInt(body, "\"age\":");
            String photo = extractString(body, "\"photoPath\":\"");

            User u = new User(username, ""); // heslo nepoznáme, netreba
            u.setId(id);
            u.setName(name);
            u.setEmail(email);
            u.setAge(age);
            u.setPhotoPath(photo);
            return u;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // ------ Feed správy ------
    // ------ Feed správy ------
    public boolean addFeedMessage(FeedMessage msg) {
        try {
            String safeTitle = msg.title == null ? "" : msg.title.replace("\"", "\\\"");
            String safeContent = msg.content == null ? "" : msg.content.replace("\"", "\\\"");
            String safePdf = msg.pdfPath == null ? "" :
                    msg.pdfPath
                            .replace("\\", "\\\\")   // C:\cesta -> C:\\cesta
                            .replace("\"", "\\\"");  // " -> \"

            String jsonReq = String.format(
                    "{\"id\":0,\"groupId\":%d,\"title\":\"%s\",\"content\":\"%s\",\"pdfPath\":\"%s\",\"createdBy\":%d}",
                    msg.groupId,
                    safeTitle,
                    safeContent,
                    safePdf,
                    msg.createdBy
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/feed"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public List<FeedMessage> getFeedMessages(int groupId) {
        List<FeedMessage> list = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/feed?groupId=" + groupId;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return list;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return list;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                int gId = extractInt(obj, "\"groupId\":");
                String title = extractString(obj, "\"title\":\"");
                String content = extractString(obj, "\"content\":\"");
                String pdf = extractString(obj, "\"pdfPath\":\"");
                int createdBy = extractInt(obj, "\"createdBy\":");
                String createdAt = extractString(obj, "\"createdAt\":\"");

                list.add(new FeedMessage(id, gId, title, content, pdf, createdBy, createdAt));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public boolean addJob(Job job) {
        try {
            String safeTitle = job.title == null ? "" : job.title.replace("\"", "\\\"");
            String safeDesc = job.description == null ? "" : job.description.replace("\"", "\\\"");

            String jsonReq = String.format(
                    "{\"id\":0,\"title\":\"%s\",\"description\":\"%s\",\"groupId\":%d," +
                            "\"status\":\"%s\",\"createdBy\":%d,\"assignedTo\":%d,\"createdAt\":\"\"}",
                    safeTitle,
                    safeDesc,
                    job.groupId,
                    job.status.replace("\"", "\\\""),
                    job.createdBy,
                    job.assignedTo
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/jobs"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateJobStatus(int jobId, String newStatus) {
        try {
            String safeStatus = newStatus == null ? "" : newStatus.replace("\"", "\\\"");
            String url = "http://localhost:8080/api/jobs/" + jobId +
                    "/status?status=" +
                    java.net.URLEncoder.encode(safeStatus, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody())
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Job> getUserJobs(int userId) {
        List<Job> jobs = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/jobs?userId=" + userId;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return jobs;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return jobs;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                String title = extractString(obj, "\"title\":\"");
                String desc = extractString(obj, "\"description\":\"");
                int groupId = extractInt(obj, "\"groupId\":");
                String status = extractString(obj, "\"status\":\"");
                int createdBy = extractInt(obj, "\"createdBy\":");
                int assignedTo = extractInt(obj, "\"assignedTo\":");
                String createdAt = extractString(obj, "\"createdAt\":\"");

                jobs.add(new Job(id, title, desc, groupId, status, createdBy, assignedTo, createdAt));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }



    public boolean updateJob(int jobId, String newTitle, String newDescription,
                             int newGroupId, String newStatus, int assignedTo) {
        try {
            String safeTitle = newTitle == null ? "" : newTitle.replace("\"", "\\\"");
            String safeDesc = newDescription == null ? "" : newDescription.replace("\"", "\\\"");
            String safeStatus = newStatus == null ? "" : newStatus.replace("\"", "\\\"");

            String jsonReq = String.format(
                    "{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\",\"groupId\":%d," +
                            "\"status\":\"%s\",\"createdBy\":0,\"assignedTo\":%d,\"createdAt\":\"\"}",
                    jobId,
                    safeTitle,
                    safeDesc,
                    newGroupId,
                    safeStatus,
                    assignedTo
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/jobs/" + jobId))
                    .header("Content-Type", "application/json")
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // PRIDA LOG
    public boolean addJobLog(JobLog log) {
        try {
            String safeWork = log.workText == null ? "" : log.workText.replace("\"", "\\\"");
            String safeCommit = log.commitMsg == null ? "" : log.commitMsg.replace("\"", "\\\"");
            String safePdf = log.pdfPath == null ? "" :
                    log.pdfPath.replace("\\", "\\\\").replace("\"", "\\\"");

            String jsonReq = String.format(
                    "{\"id\":0,\"jobId\":%d,\"userId\":%d,\"workText\":\"%s\",\"commitMsg\":\"%s\"," +
                            "\"pdfPath\":\"%s\",\"createdAt\":\"\",\"authorName\":\"\"}",
                    log.jobId,
                    log.userId,
                    safeWork,
                    safeCommit,
                    safePdf
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/jobs/logs"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // ČÍTA LOGS
    public List<JobLog> getJobLogs(int jobId) {
        List<JobLog> list = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/jobs/" + jobId + "/logs";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return list;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return list;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                int jId = extractInt(obj, "\"jobId\":");
                int userId = extractInt(obj, "\"userId\":");
                String work = extractString(obj, "\"workText\":\"");
                String commit = extractString(obj, "\"commitMsg\":\"");
                String pdf = extractString(obj, "\"pdfPath\":\"");
                String createdAt = extractString(obj, "\"createdAt\":\"");
                String author = extractString(obj, "\"authorName\":\"");

                list.add(new JobLog(id, jId, userId, work, commit, pdf, createdAt, author));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

// --------- KALENDÁR ---------

    public boolean addCalendarEvent(CalendarEvent e) {
        try {
            String safeTitle = e.title == null ? "" : e.title.replace("\"", "\\\"");
            String safeDesc = e.description == null ? "" : e.description.replace("\"", "\\\"");
            String safeColor = e.color == null ? "" : e.color.replace("\"", "\\\"");

            String jsonReq = String.format(
                    "{\"id\":0,\"groupId\":%d,\"createdBy\":%d," +
                            "\"title\":\"%s\",\"description\":\"%s\",\"date\":\"%s\"," +
                            "\"color\":\"%s\",\"notify\":%s,\"createdAt\":\"\"}",
                    e.groupId,
                    e.createdBy,
                    safeTitle,
                    safeDesc,
                    e.date,
                    safeColor,
                    e.notify ? "true" : "false"
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/calendar"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonReq))
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    //Event pre jednu skupinu
    public List<CalendarEvent> getCalendarEvents(int groupId, String yearMonth) {
        List<CalendarEvent> events = new ArrayList<>();
        try {
            String url = "http://localhost:8080/api/calendar/group?groupId=" + groupId +
                    "&yearMonth=" + java.net.URLEncoder.encode(yearMonth, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return events;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return events;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                int gId = extractInt(obj, "\"groupId\":");
                int createdBy = extractInt(obj, "\"createdBy\":");
                String title = extractString(obj, "\"title\":\"");
                String desc = extractString(obj, "\"description\":\"");
                String date = extractString(obj, "\"date\":\"");
                String color = extractString(obj, "\"color\":\"");
                boolean notify = "true".equalsIgnoreCase(extractString(obj, "\"notify\":").replace("\"",""));

                String createdAt = extractString(obj, "\"createdAt\":\"");

                events.add(new CalendarEvent(id, gId, createdBy, title, desc, date, color, notify, createdAt));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return events;
    }


    //Všetky eventy pre všetky skupiny
    public List<CalendarEvent> getCalendarEventsForUsersGroups(List<Group> userGroups,
                                                               java.time.YearMonth month) {
        List<CalendarEvent> events = new ArrayList<>();
        if (userGroups == null || userGroups.isEmpty()) return events;

        try {
            String yearMonth = month.toString(); // "2025-12"
            String groupIds = userGroups.stream()
                    .map(g -> String.valueOf(g.id))
                    .collect(java.util.stream.Collectors.joining(","));

            String url = "http://localhost:8080/api/calendar/user?yearMonth=" +
                    java.net.URLEncoder.encode(yearMonth, java.nio.charset.StandardCharsets.UTF_8) +
                    "&groupIds=" +
                    java.net.URLEncoder.encode(groupIds, java.nio.charset.StandardCharsets.UTF_8);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return events;

            String json = resp.body().trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.isBlank()) return events;

            String[] items = json.split("\\},\\s*\\{");
            for (String it : items) {
                String obj = it;
                if (!obj.startsWith("{")) obj = "{" + obj;
                if (!obj.endsWith("}")) obj = obj + "}";

                int id = extractInt(obj, "\"id\":");
                int gId = extractInt(obj, "\"groupId\":");
                int createdBy = extractInt(obj, "\"createdBy\":");
                String title = extractString(obj, "\"title\":\"");
                String desc = extractString(obj, "\"description\":\"");
                String date = extractString(obj, "\"date\":\"");
                String color = extractString(obj, "\"color\":\"");
                boolean notify = "true".equalsIgnoreCase(extractString(obj, "\"notify\":").replace("\"",""));
                String createdAt = extractString(obj, "\"createdAt\":\"");

                events.add(new CalendarEvent(id, gId, createdBy, title, desc, date, color, notify, createdAt));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return events;
    }


    private int extractInt(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        idx += key.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(idx, end)); }
        catch (Exception e) { return 0; }
    }

    private String extractString(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        idx += key.length();
        int end = json.indexOf("\"", idx);
        if (end < 0) return "";
        return json.substring(idx, end);
    }



}


