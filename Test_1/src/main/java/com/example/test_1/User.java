package com.example.test_1;

public class User {
    private String username;
    private String password;
    private String name;
    private String email;
    private int age;
    private String photoPath;
    private int id;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.name = "";
        this.email = "";
        this.age = 0;
        this.photoPath = "";
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getPhotoPath() { return photoPath; }
    public int getId() { return id; }


    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(int age) { this.age = age; }
    public void setPassword(String password) { this.password = password; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public void setId(int id) { this.id = id; }
}
