package com.dmitrromashov.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.WebhookBot;

@Component
public class StubWebhookBot implements WebhookBot {
    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public String getBotToken() {
        return null;
    }

    @Override
    public void setWebhook(String url, String publicCertificatePath) throws TelegramApiRequestException {

    }

    @Override
    public String getBotPath() {
        return null;
    }
}
