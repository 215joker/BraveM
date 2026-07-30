package com.bravem.app.domain.model;

public class Degree {
    private final String id;
    private final String name;
    private final String description;
    private final String university;

    public Degree(String id, String name, String description, String university) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.university = university;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUniversity() { return university; }
}
