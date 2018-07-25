package com.dmitrromashov.services;

import com.dmitrromashov.WeatherSubscription;
import com.dmitrromashov.WeatherState;
import com.dmitrromashov.database.DatabaseManager;
import com.dmitrromashov.network.WeatherNetworkComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class WeatherService {

    private DatabaseManager databaseManager;
    private WeatherNetworkComponent weatherNetworkComponent;

    @Autowired
    public WeatherService(DatabaseManager databaseManager, WeatherNetworkComponent weatherNetworkComponent) {
        this.databaseManager = databaseManager;
        this.weatherNetworkComponent = weatherNetworkComponent;
    }

    public void addWeatherNotificationToDb(Integer userId, String city, Integer period, String userName) throws Exception {
        databaseManager.addWeatherSubscription(userId, city, period, userName);
    }

    public List<String> getWeatherChangeMessages(String city, int period, int userId){
        List<WeatherState> weatherStates = weatherNetworkComponent.getWeatherStates(city);
        WeatherState knownWeatherState = databaseManager.getKnownWeatherState(userId, city);
        // Yandex fact weather state
        WeatherState factWeatherState = weatherStates.get(0);
        WeatherState currentWeatherState;

        boolean thereIsKnownState = false;

        if (knownWeatherState != null){
            currentWeatherState = knownWeatherState;
            thereIsKnownState = true;
        } else {
            currentWeatherState = factWeatherState;
        }

        int periodInSeconds = period * 3600 ;


        String messageText = "";
        List<String> messagesList = new ArrayList<>();

        for (int i = 0; i < weatherStates.size(); i++) {
            WeatherState weatherState = weatherStates.get(i);
            int weatherStateTime = weatherState.getTime();
            String weatherStateCondition = weatherState.getWeatherCondition();

            long currentTimeInSeconds = currentWeatherState.getTime();
            String currentWeatherCondition = currentWeatherState.getWeatherCondition();

            if (weatherStateTime > currentTimeInSeconds
                    && weatherStateTime <= currentTimeInSeconds + periodInSeconds
                    && !currentWeatherCondition.equals(weatherStateCondition)){
                String hourWord;
                if (period == 1){
                    hourWord = "часа";
                } else {
                    hourWord = "часов";
                }

                messageText = "в городе " + city + " в течение " + period + " " + hourWord +
                        " произойдёт изменение погоды на \"" + weatherStateCondition + "\"";

                messagesList.add(messageText);
                currentWeatherState.setTime(weatherStateTime);
                currentWeatherState.setWeatherCondition(weatherStateCondition);
                periodInSeconds -= 3600;

            }
        }

        if (thereIsKnownState){
            databaseManager.updateKnownWeatherState(userId, city, currentWeatherState.getWeatherCondition(), currentWeatherState.getTime());
        } else {
            databaseManager.addKnownWeatherState(userId, city, currentWeatherState.getWeatherCondition(), currentWeatherState.getTime());
        }

        return messagesList;
    }

    public List<WeatherSubscription> getAllWeatherSubscriptions(){
        return databaseManager.getAllWeatherSucscriptions();

    }

    public void unsubscribeUser(int userId) {
        databaseManager.deleteUserSubscription(userId);
        databaseManager.deleteUserWeatherState(userId);

    }
}
