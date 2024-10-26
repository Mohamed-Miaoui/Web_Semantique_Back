package com.example.jardin_urbain_ws;

// type(string), price(double), availability(boolean)
public class Seed {

    private String type;
    private Double price;
    private Boolean availability;

    public Seed(String type, Double price, Boolean availability) {
        this.type = type;
        this.price = price;
        this.availability = availability;
    }

    public Seed() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }
}
