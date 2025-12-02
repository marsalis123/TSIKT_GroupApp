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
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO jobs (title, description, group_id, status, created_by, assigned_to) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, job.title);
            pstmt.setString(2, job.description);
            pstmt.setInt(3, job.groupId);
            pstmt.setString(4, job.status);
            pstmt.setInt(5, job.createdBy);
            pstmt.setInt(6, job.assignedTo);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateJobStatus(int jobId, String newStatus) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "UPDATE jobs SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, jobId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Job> getUserJobs(int userId) {
        List<Job> jobs = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            // Najskôr zisti zoznam group_id kde je user člen (alebo owner)
            String sqlGroups = "SELECT g.id FROM groups g "
                    + "LEFT JOIN group_members gm ON g.id = gm.group_id "
                    + "WHERE g.owner = (SELECT username FROM users WHERE id = ?) OR gm.user_id = ?";
            PreparedStatement stmtGroups = conn.prepareStatement(sqlGroups);
            stmtGroups.setInt(1, userId);
            stmtGroups.setInt(2, userId);
            ResultSet rsGroups = stmtGroups.executeQuery();
            List<Integer> groupIds = new ArrayList<>();
            while (rsGroups.next()) groupIds.add(rsGroups.getInt(1));

            if (groupIds.isEmpty()) return jobs;

            // Dynamicky priprav zoznam id do SQL
            String inClause = groupIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
            String sqlJobs = "SELECT * FROM jobs WHERE group_id IN (" + inClause + ")";
            PreparedStatement stmtJobs = conn.prepareStatement(sqlJobs);

            ResultSet rsJobs = stmtJobs.executeQuery();
            while (rsJobs.next()) {
                jobs.add(new Job(
                        rsJobs.getInt("id"),
                        rsJobs.getString("title"),
                        rsJobs.getString("description"),
                        rsJobs.getInt("group_id"),
                        rsJobs.getString("status"),
                        rsJobs.getInt("created_by"),
                        rsJobs.getInt("assigned_to"),
                        rsJobs.getString("created_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return jobs;
    }


    public boolean updateJob(int jobId, String newTitle, String newDescription, int newGroupId, String newStatus, int assignedTo) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "UPDATE jobs SET title = ?, description = ?, group_id = ?, status = ?, assigned_to = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newDescription);
            pstmt.setInt(3, newGroupId);
            pstmt.setString(4, newStatus);
            pstmt.setInt(5, assignedTo);
            pstmt.setInt(6, jobId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // PRIDA LOG
    public boolean addJobLog(JobLog log) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO job_logs (job_id, user_id, work_text, commit_msg, pdf_path) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, log.jobId);
            pstmt.setInt(2, log.userId);
            pstmt.setString(3, log.workText);
            pstmt.setString(4, log.commitMsg);
            pstmt.setString(5, log.pdfPath);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ČÍTA LOGS (s menom autora)
    public List<JobLog> getJobLogs(int jobId) {
        List<JobLog> list = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT l.*, u.name as authorName FROM job_logs l JOIN users u ON l.user_id = u.id WHERE l.job_id = ? ORDER BY l.created_at ASC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, jobId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new JobLog(
                        rs.getInt("id"),
                        rs.getInt("job_id"),
                        rs.getInt("user_id"),
                        rs.getString("work_text"),
                        rs.getString("commit_msg"),
                        rs.getString("pdf_path"),
                        rs.getString("created_at"),
                        rs.getString("authorName")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
// --------- KALENDÁR ---------

    public boolean addCalendarEvent(CalendarEvent e) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO calendar_events (group_id, created_by, title, description, date, color, notify) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, e.groupId);
            pstmt.setInt(2, e.createdBy);
            pstmt.setString(3, e.title);
            pstmt.setString(4, e.description);
            pstmt.setString(5, e.date);
            pstmt.setString(6, e.color);
            pstmt.setInt(7, e.notify ? 1 : 0);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // Get all events for group and month ("2025-11")
    public List<CalendarEvent> getCalendarEvents(int groupId, String yearMonth) {
        List<CalendarEvent> events = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT * FROM calendar_events WHERE group_id = ? AND substr(date, 1, 7) = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, yearMonth);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(new CalendarEvent(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        rs.getInt("created_by"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("color"),
                        rs.getInt("notify") != 0,
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return events;
    }

    // Get all events for ALL user's groups for a month
    public List<CalendarEvent> getCalendarEventsForUsersGroups(List<Group> userGroups, java.time.YearMonth month) {
        List<CalendarEvent> events = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            if (userGroups == null || userGroups.isEmpty()) return events;
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM calendar_events WHERE (");
            for (int i=0;i<userGroups.size();i++) {
                sb.append("group_id=?");
                if (i < userGroups.size()-1) sb.append(" OR ");
            }
            sb.append(") AND substr(date, 1, 7) = ?");
            PreparedStatement ps = conn.prepareStatement(sb.toString());
            for (int i=0; i<userGroups.size();i++)
                ps.setInt(i+1, userGroups.get(i).id);
            ps.setString(userGroups.size()+1, month.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                events.add(new CalendarEvent(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        rs.getInt("created_by"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("color"),
                        rs.getInt("notify") != 0,
                        rs.getString("created_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
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


