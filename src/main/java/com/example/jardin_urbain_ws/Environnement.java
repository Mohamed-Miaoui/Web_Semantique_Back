package com.example.jardin_urbain_ws;

//light_conditions(string), temperature(double), humidity(double)
public class Environnement {

    private String light_conditions;
    private Double temperature;
    private Double humidity;

    public Environnement(String light_conditions, Double temperature, Double humidity) {
        this.light_conditions = light_conditions;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public Environnement() {
    }

    public String getLight_conditions() {
        return light_conditions;
    }

    public void setLight_conditions(String light_conditions) {
        this.light_conditions = light_conditions;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

}
