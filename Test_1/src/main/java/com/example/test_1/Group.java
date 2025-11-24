package com.example.test_1;

public class Group {
    public int id;
    public String name;
    public String owner;

    // EXISTUJE: (int, String, String)
    public Group(int id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
    // EXISTUJE: (String, String)
    public Group(String name, String owner) {
        this(-1, name, owner);
    }
    // DOPLŇ TOTO: (int, String)
    public Group(int id, String name) {
        this.id = id;
        this.name = name;
        this.owner = "";
    }

    @Override public String toString() {
        return name;
    }
}
