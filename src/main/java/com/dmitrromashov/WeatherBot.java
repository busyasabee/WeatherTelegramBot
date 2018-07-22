package com.dmitrromashov;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class WeatherBot extends TelegramLongPollingBot {
    private static final String beginCommandSymbol = "/";
    private static final String startCommandName = "start";
    private static final String subscribeCommandName = "subscribe";
    private static final String unsubscribeCommandName = "unsubscribe";

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        processIncomingMessage(message);

    }

    private void processIncomingMessage(Message incomingMessage){
        String messageText = incomingMessage.getText();
        if (messageText.equals(beginCommandSymbol + startCommandName)){
            processStartCommand(incomingMessage);
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
        startSendMessage.setText("Choose command");
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
