package com.example.jardin_urbain_ws;

public class Review {
    private String user;
    private String content;
    private String date;

    public Review() {
    }

    public Review(String user, String content, String date) {
        this.user = user;
        this.content = content;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
