package com.dmitrromashov.bot;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WeatherBot extends TelegramLongPollingBot {
    private static final String beginCommandSymbol = "/";
    private static final String startCommandName = "start";
    private static final String subscribeCommandName = "subscribe";
    private static final String unsubscribeCommandName = "unsubscribe";

    private WeatherService weatherService;

    @Autowired
    public WeatherBot(WeatherService weatherService) {
        this.weatherService = weatherService;

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
        }

    }

    private void processSubscribeCommand(Message subscribeMessage) {
        Long chatId = subscribeMessage.getChatId();
        Integer messageId = subscribeMessage.getMessageId();
        Integer userId = subscribeMessage.getFrom().getId();
        String text = subscribeMessage.getText().trim();

        if (text.equals("subscribe") || text.equals("/subscribe")){
            sendHelpMessageToSubscribe(chatId, messageId);
        } else {
            String patternStr = "/subscribe\\s+(\\p{L}+\\s?-?\\p{L}*)\\s*,\\s*(\\d+)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            String city;
            String period;

            if(matcher.find()){
                city = matcher.group(1);
                period = matcher.group(2);

                subscribeToNotifications(chatId, messageId, userId, city, period);
            } else {
                sendMessageAboutNotification(chatId, messageId, false);
            }
        }

    }

    private void subscribeToNotifications(Long chatId, Integer messageId, Integer userId, String city, String period){
        boolean notificationAdded = false;

        try {
            weatherService.addWeatherNotificationToDb(userId, city, period);
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
            text = "Уведомление было успешно добавлено";
        } else {
            text = "При добавлении уведомления возникли проблемы. Пожалуйста, проверьте правильность введённых данных";
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
