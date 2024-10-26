package com.example.jardin_urbain_ws;

//Attributes: name(string), phone(integer), location(string)
public class Store {
    private String name;
    private Integer phone;
    private String location;

    public Store() {
    }

    public Store(String name, Integer phone, String location) {
        this.name = name;
        this.phone = phone;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
