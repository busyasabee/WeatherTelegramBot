package com.dmitrromashov;

public class WeatherState {
    private String weatherCondition;
    private int time;

    public WeatherState(String weatherCondition, int time) {
        this.weatherCondition = weatherCondition;
        this.time = time;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
