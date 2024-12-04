package org.registry.akashic.akashicjavafx.domain;

public class Book {
    private int id;
    private String title;
    private String author;
    private String description;
    private String imageName;

    public Book(String title, String author, String description, String imageName) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageName = imageName;
    }

    public Book(int id, String title, String author, String description, String imageName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageName = imageName;
    }
}