package com.example.jardin_urbain_ws;

public class Tutorial {
    private String title;
    private String content;
    private Integer estimated_time;

    public Tutorial() {
    }

    public Tutorial(String title, String content, Integer estimated_time) {
        this.title = title;
        this.content = content;
        this.estimated_time = estimated_time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEstimated_time() {
        return estimated_time;
    }

    public void setEstimated_time(Integer estimated_time) {
        this.estimated_time = estimated_time;
    }
}
