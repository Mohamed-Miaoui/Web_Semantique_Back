package com.example.jardin_urbain_ws;

public class Sponsor {
    private String name;
    private String industry;
    private Integer phone;

    public Sponsor() {
    }

    public Sponsor(String name, String industry, Integer phone) {
        this.name = name;
        this.industry = industry;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }
}
