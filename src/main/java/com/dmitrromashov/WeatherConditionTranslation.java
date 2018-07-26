package com.dmitrromashov;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeatherConditionTranslation {
    private Map<String, String>  translationMap;
    private static final String FILE_NAME = "translations.txt";

    public WeatherConditionTranslation() {
        translationMap = new HashMap<>();
        fillMap();
    }

    public void fillMap(){
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(FILE_NAME), StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splittedLine = line.split(" — ");
                String eng = splittedLine[0].trim();
                String rus = splittedLine[1].trim();
                translationMap.put(eng, rus);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTranslation(String key){
        return translationMap.get(key);
    }
}
