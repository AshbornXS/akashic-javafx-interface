package org.registry.akashic.akashicjavafx.response;

import java.util.List;

public class APIResponseBody {
    private List<Book> content;
    private int totalPages;
    private int totalElements;
    private int number;
    private int size;

    public List<Book> getContent() {
        return content;
    }

    public void setContent(List<Book> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}