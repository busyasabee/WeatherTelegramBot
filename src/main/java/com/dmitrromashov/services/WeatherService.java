package com.dmitrromashov.services;

import com.dmitrromashov.database.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class WeatherService {

    private DatabaseManager databaseManager;

    @Autowired
    public WeatherService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addWeatherNotificationToDb(Integer userId, String city, String period) throws Exception {
        int periodInt = Integer.parseInt(period);
        databaseManager.addWeatherNotification(userId, city, periodInt);
    }
}
