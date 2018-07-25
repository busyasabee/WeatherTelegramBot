package com.dmitrromashov;

public class WeatherSubscription {
    private int userId;
    private String city;
    private String userName;
    private int period;

    public WeatherSubscription(int userId, String city, int period) {
        this.userId = userId;
        this.city = city;
        this.period = period;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
