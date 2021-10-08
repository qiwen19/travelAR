package com.ict3104.t10_idk_2020.model;

public class Weather {
    private String temp;
    private String weather;
    private String iconCode;
    public Weather(){}

    public String getTemp(){
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getWeather(){ return weather; }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getIconCode(){ return iconCode; }

    public void setIconCode(String iconCode){ this.iconCode = iconCode; }
}
