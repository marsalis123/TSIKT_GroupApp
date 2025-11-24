package com.example.test_1;

import java.sql.*;
import java.util.*;

public class UserManager {

    public UserManager() {}

    public boolean register(String username, String password) {
        try (Connection conn = DBManager.getConnection()) {
            java.util.Random rand = new java.util.Random();
            int newId = rand.nextInt(90000) + 10000;
            String sql = "INSERT INTO users (username, password, name, email, age, photo, id) VALUES (?, ?, '', '', 0, '', ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setInt(3, newId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public User login(String username, String password) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getString("username"), password);
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setAge(rs.getInt("age"));
                user.setPhotoPath(rs.getString("photo"));
                user.setId(rs.getInt("id"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveUser(User user) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "UPDATE users SET name = ?, email = ?, age = ?, photo = ? WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getAge());
            pstmt.setString(4, user.getPhotoPath());
            pstmt.setString(5, user.getUsername());
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ZDIEĽANÉ SKUPINY – nájde všetky, ktorých je užívateľ členom alebo je vlastník
    public List<Group> getUserGroups(String username) {
        List<Group> groups = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            // najdi id usera podľa username
            String sqlUser = "SELECT id FROM users WHERE username = ?";
            PreparedStatement p = conn.prepareStatement(sqlUser);
            p.setString(1, username);
            ResultSet rs = p.executeQuery();
            int uid = rs.next() ? rs.getInt("id") : -1;

            // skupiny kde je user člen alebo vlastník (LEFT JOIN zabezpečí že vlastníctvo je vždy zahrnuté)
            String sql = "SELECT DISTINCT g.* FROM groups g " +
                    "LEFT JOIN group_members m ON g.id = m.group_id " +
                    "WHERE g.owner = ? OR m.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setInt(2, uid);

            ResultSet groupsRs = pstmt.executeQuery();
            while (groupsRs.next()) {
                groups.add(new Group(groupsRs.getInt("id"),
                        groupsRs.getString("name"),
                        groupsRs.getString("owner")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }

    public boolean addGroup(Group group) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO groups (name, owner) VALUES (?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, group.name);
            pstmt.setString(2, group.owner);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGroup(int groupId) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "DELETE FROM groups WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.executeUpdate();
            // Odstrániť aj členstvá (čistota)
            PreparedStatement pm = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ?");
            pm.setInt(1, groupId);
            pm.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Správne pracuje aj ak užívateľ je už člen v skupine (duplicitné členstvo ignoruje)
    public boolean addUserToGroup(int groupId, int userId) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT OR IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeUserFromGroup(int groupId, int userId) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT u.* FROM users u " +
                    "JOIN group_members gm ON u.id = gm.user_id " +
                    "WHERE gm.group_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User u = new User(rs.getString("username"), rs.getString("password"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setAge(rs.getInt("age"));
                u.setPhotoPath(rs.getString("photo"));
                u.setId(rs.getInt("id"));
                members.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public User findUser(String value) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? OR email = ? OR id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            pstmt.setString(2, value);
            try {
                pstmt.setInt(3, Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                pstmt.setInt(3, -1);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setAge(rs.getInt("age"));
                user.setPhotoPath(rs.getString("photo"));
                user.setId(rs.getInt("id"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------ Feed správy ------
    public boolean addFeedMessage(FeedMessage msg) {
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO feed_messages (group_id, title, content, pdf_path, created_by) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, msg.groupId);
            pstmt.setString(2, msg.title);
            pstmt.setString(3, msg.content);
            pstmt.setString(4, msg.pdfPath);
            pstmt.setInt(5, msg.createdBy);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FeedMessage> getFeedMessages(int groupId) {
        List<FeedMessage> list = new ArrayList<>();
        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT * FROM feed_messages WHERE group_id = ? ORDER BY created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new FeedMessage(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("pdf_path"),
                        rs.getInt("created_by"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
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


}


