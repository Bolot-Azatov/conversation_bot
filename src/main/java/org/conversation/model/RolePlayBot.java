package org.conversation.model;

import lombok.extern.slf4j.Slf4j;
import org.conversation.dto.ChatDto;
import org.conversation.service.AiService;
import org.conversation.service.KeyboardFactory;
import org.conversation.service.UserSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class RolePlayBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final UserSessionService sessionService;
    private final AiService aiService;

    private static final long INACTIVITY_THRESHOLD_MS = 30 * 60 * 1000L;

    public RolePlayBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UserSessionService sessionService,
            AiService aiService) {
        super(botToken);
        this.botUsername = botUsername;
        this.sessionService = sessionService;
        this.aiService = aiService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();
        String text = update.getMessage().getText().trim();

        sessionService.trackMessage(chatId, messageId);

        log.info("Сообщение от chatId [{}]: {}", chatId, text);

        if (text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("/reset") || text.equals("🔄 Сменить собеседника")) {
            sessionService.resetSession(chatId);
            sendStartMenu(chatId);
            return;
        }

        if (text.equals("🗑 Удалить сообщения")) {
            handleDeleteAllMessages(chatId, true);
            return;
        }

        if (text.equals("🧠 Сбросить память")) {
            if (!sessionService.hasPersona(chatId)) {
                sendStartMenu(chatId);
                return;
            }
            sessionService.clearHistoryKeepPersona(chatId);
            sendMessage(chatId, "🧠 Память ИИ очищена! Собеседник *" + sessionService.getPersona(chatId) + "* готов начать заново.", KeyboardFactory.createMainChatKeyboard());
            return;
        }

        if (text.equals("👤 Кто собеседник?")) {
            if (!sessionService.hasPersona(chatId)) {
                sendMessage(chatId, "Собеседник еще не выбран. Напиши имя или выбери из меню ниже:", KeyboardFactory.createPopularPersonasKeyboard());
                return;
            }
            String persona = sessionService.getPersona(chatId);
            int count = sessionService.getUserMessageCount(chatId);
            sendMessage(chatId, "👤 Текущий собеседник: *" + persona + "*\n💬 Сообщений в памяти: " + count, KeyboardFactory.createMainChatKeyboard());
            return;
        }

        if (text.equals("💡 О чем спросить?")) {
            handleSuggestTopics(chatId);
            return;
        }

        if (text.equals("📜 Досье")) {
            handleShowDossier(chatId);
            return;
        }

        if (!sessionService.hasPersona(chatId)) {
            startChatWithPersona(chatId, text);
            return;
        }

        sessionService.addMessage(chatId, "user", text);
        String aiReply = aiService.generateResponse(sessionService.getHistory(chatId));
        sessionService.addMessage(chatId, "assistant", aiReply);

        sendMessage(chatId, aiReply, KeyboardFactory.createMainChatKeyboard());
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));
        } catch (TelegramApiException e) {
            log.error("Ошибка answerCallbackQuery", e);
        }

        if (callbackData.startsWith("persona:")) {
            String persona = callbackData.substring("persona:".length());
            if ("RANDOM".equals(persona)) {
                persona = sessionService.getRandomPersona();
            }
            startChatWithPersona(chatId, persona);
        }
    }

    private void startChatWithPersona(Long chatId, String persona) {
        sessionService.setPersona(chatId, persona);
        String welcome = "🎭 Отлично! Твой собеседник — *" + persona + "*.\n\nЗадай ему свой вопрос или выбери действие в меню:";
        sendMessage(chatId, welcome, KeyboardFactory.createMainChatKeyboard());
    }

    private void sendStartMenu(Long chatId) {
        String text = "Привет! С кем ты хочешь поговорить?\n\n" +
                "Выбери готового персонажа кнопкой ниже или просто *напиши любое имя* в чат (например: _Илон Маск_, _Петр I_):";
        sendMessage(chatId, text, KeyboardFactory.createPopularPersonasKeyboard());
    }

    private void handleDeleteAllMessages(Long chatId, boolean notifyUser) {
        List<Integer> messageIds = sessionService.getTrackedMessageIds(chatId);
        for (Integer msgId : messageIds) {
            try {
                execute(new DeleteMessage(chatId.toString(), msgId));
            } catch (TelegramApiException ignored) {
            }
        }
        sessionService.clearTrackedMessages(chatId);

        if (notifyUser) {
            if (sessionService.hasPersona(chatId)) {
                sendMessage(chatId, "🗑 Чат очищен! Вы по-прежнему общаетесь с *" + sessionService.getPersona(chatId) + "*.", KeyboardFactory.createMainChatKeyboard());
            } else {
                sendStartMenu(chatId);
            }
        }
    }

    private void handleSuggestTopics(Long chatId) {
        if (!sessionService.hasPersona(chatId)) {
            sendStartMenu(chatId);
            return;
        }
        String persona = sessionService.getPersona(chatId);
        List<ChatDto.Message> prompt = List.of(
                new ChatDto.Message("user", "Предложи ровно 3 интересных, глубоких и оригинальных вопроса или темы, которые можно обсудить с " + persona + ". Оформи кратким списком с эмодзи.")
        );
        String suggestions = aiService.generateResponse(prompt);
        sendMessage(chatId, "💡 *Темы для беседы с " + persona + ":*\n\n" + suggestions, KeyboardFactory.createMainChatKeyboard());
    }

    private void handleShowDossier(Long chatId) {
        if (!sessionService.hasPersona(chatId)) {
            sendStartMenu(chatId);
            return;
        }
        String persona = sessionService.getPersona(chatId);
        List<ChatDto.Message> prompt = List.of(
                new ChatDto.Message("user", "Напиши краткое и емкое историческое досье о личности " + persona + ": годы жизни/эпоха, главные достижения, ключевые факты и след в истории (не более 4-5 предложений).")
        );
        String dossier = aiService.generateResponse(prompt);
        sendMessage(chatId, "📜 *Историческое досье: " + persona + "*\n\n" + dossier, KeyboardFactory.createMainChatKeyboard());
    }

    private void sendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown");

        if (keyboard != null) {
            builder.replyMarkup(keyboard);
        }

        try {
            Message sentMessage = execute(builder.build());
            sessionService.trackMessage(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            try {
                SendMessage fallback = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text(text)
                        .replyMarkup(keyboard)
                        .build();
                Message sentFallback = execute(fallback);
                sessionService.trackMessage(chatId, sentFallback.getMessageId());
            } catch (TelegramApiException ex) {
                log.error("Не удалось отправить сообщение: {}", ex.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    public void scheduledChatAutoCleanup() {
        List<Long> inactiveChats = sessionService.getInactiveChatIds(INACTIVITY_THRESHOLD_MS);
        for (Long chatId : inactiveChats) {
            log.info("⏰ Авто-очистка неактивного чата [chatId: {}]", chatId);
            handleDeleteAllMessages(chatId, false);
            sessionService.clearHistoryKeepPersona(chatId);
        }
    }
}