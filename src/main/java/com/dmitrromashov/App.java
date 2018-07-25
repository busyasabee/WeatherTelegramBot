package com.dmitrromashov;


import com.dmitrromashov.bot.WeatherBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.starter.EnableTelegramBots;

@SpringBootApplication
@EnableTelegramBots
public class App 
{
    private static WeatherBot weatherBot = null;

    @Autowired
    public App(WeatherBot weatherBot) {
        App.weatherBot = weatherBot;
    }


    public static void main( String[] args )
    {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        SpringApplication.run(App.class, args);

        try {
            telegramBotsApi.registerBot(weatherBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
