package com.example.jardin_urbain_ws;

//name(string), type(string), water_needs(double)
public class Plant {
    private String name;
    private String type;
    private Double water_needs;

    public Plant(String name, String type, Double water_needs) {
        this.name = name;
        this.type = type;
        this.water_needs = water_needs;
    }

    public Plant() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getWater_needs() {
        return water_needs;
    }

    public void setWater_needs(Double water_needs) {
        this.water_needs = water_needs;
    }
}
