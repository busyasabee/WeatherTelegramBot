package com.dmitrromashov.bot;

import com.dmitrromashov.Timer;
import com.dmitrromashov.WeatherSubscription;
import com.dmitrromashov.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WeatherBot extends TelegramLongPollingBot {
    private static final String beginCommandSymbol = "/";
    private static final String startCommandName = "start";
    private static final String subscribeCommandName = "subscribe";
    private static final String unsubscribeCommandName = "unsubscribe";

    private WeatherService weatherService;
    private Timer notificationTimer;

    @Autowired
    public WeatherBot(WeatherService weatherService, Timer timer) {
        this.weatherService = weatherService;
        notificationTimer = timer;
        startNotificationTimer();

    }

    private void startNotificationTimer(){
        List<WeatherSubscription> weatherSubscriptions = weatherService.getAllWeatherSubscriptions();

        for (WeatherSubscription weatherSubscription: weatherSubscriptions){
            int userId = weatherSubscription.getUserId();
            String city = weatherSubscription.getCity();
            String userName = weatherSubscription.getUserName();
            int period = weatherSubscription.getPeriod();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    List<String> weatherChangeList = weatherService.getWeatherChangeMessages(city, period, userId);
                    if (weatherChangeList.size() != 0){
                        sendMessagesAboutWeatherChange(weatherChangeList, userId, userName);
                    }

                }
            };

            notificationTimer.start(runnable, 0, 3600, TimeUnit.SECONDS);

        }


    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        processIncomingMessage(message);

    }

    private void processIncomingMessage(Message incomingMessage){
        String messageText = incomingMessage.getText();
        if (messageText.equals(beginCommandSymbol + startCommandName)){
            processStartCommand(incomingMessage);
        } else if (messageText.startsWith(beginCommandSymbol + subscribeCommandName) || messageText.startsWith(subscribeCommandName)){
            processSubscribeCommand(incomingMessage);
        } else if (messageText.startsWith(beginCommandSymbol + unsubscribeCommandName) || messageText.startsWith(unsubscribeCommandName)){
            processUnsubscribeCommand(incomingMessage);
        }

    }

    private void processUnsubscribeCommand(Message unsubscribeMessage){
        Long chatId = unsubscribeMessage.getChatId();
        Integer messageId = unsubscribeMessage.getMessageId();
        Integer userId = unsubscribeMessage.getFrom().getId();
        String text = unsubscribeMessage.getText().trim();

        if (text.equals("unsubscribe") || text.equals("/unsubscribe")){
            weatherService.unsubscribeUser(userId);
            sendHelpMessageToUnsubscribe(chatId, messageId);
        }

    }

    private void sendHelpMessageToUnsubscribe(Long chatId, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        String helpText = "Вы успешно отписались от уведомлений об изменениях погоды";
        sendMessage.setText(helpText);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processSubscribeCommand(Message subscribeMessage) {
        Long chatId = subscribeMessage.getChatId();
        Integer messageId = subscribeMessage.getMessageId();
        Integer userId = subscribeMessage.getFrom().getId();
        String text = subscribeMessage.getText().trim();
        String userName = subscribeMessage.getFrom().getUserName();

        if (text.equals("subscribe") || text.equals("/subscribe")){
            sendHelpMessageToSubscribe(chatId, messageId);
        } else {
            String patternStr = "/subscribe\\s+(\\p{L}+\\s?-?\\p{L}*)\\s*,\\s*(\\d+)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            String city;
            Integer period;

            if(matcher.find()){
                city = matcher.group(1);
                period = Integer.parseInt(matcher.group(2));

                subscribeToNotifications(chatId, messageId, userId, city, period, userName);

                List<String> weatherChangeList = weatherService.getWeatherChangeMessages(city, period, userId);
                if (weatherChangeList.size() != 0){
                    sendMessagesAboutWeatherChange(weatherChangeList, chatId, userName);
                }
            } else {
                sendMessageAboutNotification(chatId, messageId, false);
            }

        }
    }

    private void sendMessagesAboutWeatherChange(List<String> weatherChangeList, long chatId, String userName){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String text = userName;
        String firstChange = weatherChangeList.get(0);
        text = text + ", " + firstChange;
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < weatherChangeList.size(); i++) {
            text = "\n После " + weatherChangeList.get(i);
            sendMessage.setText(text);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void subscribeToNotifications(Long chatId, Integer messageId, Integer userId, String city, Integer period, String userName){
        boolean notificationAdded = false;

        try {
            weatherService.addWeatherNotificationToDb(userId, city, period, userName);
            notificationAdded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageAboutNotification(chatId, messageId, notificationAdded);
    }

    private void sendMessageAboutNotification(Long chatId, Integer messageId, boolean notificationAdded){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        String text;
        if (notificationAdded){
            text = "Вы были успешно подписаны на уведомления об изменениях погоды";
        } else {
            text = "С подпиской на уведомления возникли проблемы. Пожалуйста, проверьте правильность введённых данных";
        }
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendHelpMessageToSubscribe(Long chatId, Integer messageId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        String helpText = "Пожалуйста, выберите город и период уведомления. Пример: /subscribe Санкт-Петербург,1. После этого вы будете получать уведомления об изменениях погоды в городе Санкт-Петербург с периодом времени в 1 час";
        sendMessage.setText(helpText);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    private void processStartCommand(Message startMessage){
        Long chatId = startMessage.getChatId();
        Integer messageId = startMessage.getMessageId();
        ReplyKeyboardMarkup replyKeyboardMarkup = getKeybord();
        SendMessage startSendMessage = new SendMessage();
        startSendMessage.enableMarkdown(true);
        startSendMessage.setChatId(chatId);
        startSendMessage.setReplyToMessageId(messageId);
        startSendMessage.setReplyMarkup(replyKeyboardMarkup);
        startSendMessage.setText("Пожалуйста, выберите команду");
        try {
            execute(startSendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private ReplyKeyboardMarkup getKeybord() {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(subscribeCommandName);
        keyboardRow.add(unsubscribeCommandName);

        keyboard.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }


    @Override
    public String getBotUsername() {
        return "DmitrRomWeatherBot";
    }

    @Override
    public String getBotToken() {
        return "596528454:AAGvu63Z7o-pipyqI1Qi__sNizHwsz_LbjI";
    }
}
