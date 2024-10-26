package com.example.jardin_urbain_ws;

// name(string), type(string), quantity(integer)
public class Materiel {
    private String name;
    private String type;
    private Integer quantity;

    public Materiel(String name, String type, Integer quantity) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
    }

    public Materiel() {
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
