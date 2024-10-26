package com.example.jardin_urbain_ws;

//name(string), size(integer), location(string)

public class Garden {
    private String name;
    private Integer size;
    private String location;

    public Garden(String name, Integer size, String location) {
        this.name = name;
        this.size = size;
        this.location = location;
    }

    public Garden() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
