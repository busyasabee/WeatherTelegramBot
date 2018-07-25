package com.dmitrromashov.network;

import com.dmitrromashov.CityCoordinates;
import com.dmitrromashov.WeatherConditionTranslation;
import com.dmitrromashov.WeatherState;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherNetworkComponent {

    private WeatherConditionTranslation weatherConditionTranslation;

    @Autowired
    public WeatherNetworkComponent(WeatherConditionTranslation weatherConditionTranslation){
        this.weatherConditionTranslation = weatherConditionTranslation;
    }

    private CityCoordinates getCityCoordinates(String city){
        CityCoordinates cityCoordinates = null;
        try (CloseableHttpClient client = HttpClients.createDefault();){
            String baseUrl = "https://geocode-maps.yandex.ru/1.x/";
            String format = "json";
            String url = baseUrl + "?format=" + format + "&geocode=" + city;
            HttpGet httpGet = new HttpGet(url);
            HttpEntity httpEntity;
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response status " + statusCode);
                if (statusCode == 200){
                    httpEntity = response.getEntity();
                    String responseString = EntityUtils.toString(httpEntity, "UTF-8");

                    JSONObject jsonObject = new JSONObject(responseString);
                    String pos = jsonObject
                            .getJSONObject("response")
                            .getJSONObject("GeoObjectCollection")
                            .getJSONArray("featureMember")
                            .getJSONObject(0)
                            .getJSONObject("GeoObject")
                            .getJSONObject("Point")
                            .getString("pos");

                    String[] coords = pos.split(" ");
                    cityCoordinates = new CityCoordinates(coords[0], coords[1]);
                    System.out.println("City = " + city);
                    System.out.println("lat = " + cityCoordinates.getLatitude());
                    System.out.println("lon = " + cityCoordinates.getLongitude());

                } else {
                    System.out.println("Error happened during request coordinates");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityCoordinates;
    }

    private List<WeatherState> requestWeatherStates(CityCoordinates cityCoordinates){
        String apiKey = "62d591b5-8f51-40d7-a6c6-a1ecb55beb16";
        List<WeatherState> weatherStates = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault();){
            String baseUrl = "https://api.weather.yandex.ru/v1/forecast";
            String lat = cityCoordinates.getLatitude();
            String lon = cityCoordinates.getLongitude();
            String dayLimit = "1";
            String url = baseUrl + "?lat=" + lat + "&lon=" + lon + "&limit=" + dayLimit;

            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("X-Yandex-API-Key", apiKey);
            HttpEntity httpEntity;


            try (CloseableHttpResponse response = client.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response status " + statusCode);
                if (statusCode == 200){
                    httpEntity = response.getEntity();
                    String responseString = EntityUtils.toString(httpEntity, "UTF-8");

                    JSONObject jsonResponse = new JSONObject(responseString);
                    JSONObject fact = jsonResponse
                            .getJSONObject("fact");
                    String factCondition = fact.getString("condition");
                    factCondition = weatherConditionTranslation.getTranslation(factCondition);
                    int factTime = fact.getInt("obs_time");
                    weatherStates.add(new WeatherState(factCondition, factTime));

                    System.out.println("Fact condition = " + factCondition + " fact_time = " + factTime);
                    JSONArray forecasts = jsonResponse
                            .getJSONArray("forecasts");
                    JSONArray hours = forecasts
                            .getJSONObject(0)
                            .getJSONArray("hours");


                    for (int i = 0; i < hours.length(); i++) {
                        JSONObject hour = hours.getJSONObject(i);
                        int hourTime = hour.getInt("hour_ts");
                        String condition = hour.getString("condition");
                        condition = weatherConditionTranslation.getTranslation(condition);
                        WeatherState weatherState = new WeatherState(condition, hourTime);
                        weatherStates.add(weatherState);
                        System.out.println("hour_ts = " + hourTime + " condition = " + condition);
                    }

                } else {
                    System.out.println("Error happened during request");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return weatherStates;

    }

    public List<WeatherState> getWeatherStates(String city){
        CityCoordinates cityCoordinates = getCityCoordinates(city);
        return requestWeatherStates(cityCoordinates);

    }
}
