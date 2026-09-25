package org.conversation.config;

import org.conversation.model.RolePlayBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(RolePlayBot rolePlayBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(rolePlayBot);
        return telegramBotsApi;
    }
}