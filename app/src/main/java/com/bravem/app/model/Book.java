package com.bravem.app.model;

import java.io.Serializable;

public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private boolean available;
    private String description;

    public Book() {}

    public Book(String id, String title, String author, String category, String isbn, boolean available, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.available = available;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return available; }
    public String getDescription() { return description; }
}
