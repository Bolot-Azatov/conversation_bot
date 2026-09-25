package org.conversation.service;

import lombok.Getter;
import lombok.Setter;
import org.conversation.dto.ChatDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    private final Map<Long, SessionData> sessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static final List<String> RANDOM_PERSONAS = List.of(
            "Леонардо да Винчи", "Юлий Цезарь", "Клеопатра", "Наполеон Бонапарт",
            "Стив Джобс", "Конфуций", "Зигмунд Фрейд", "Галилео Галилей",
            "Марк Аврелий", "Артур Конан Дойл", "Чингисхан", "Фрида Кало"
    );

    @Getter
    @Setter
    public static class SessionData {
        private String persona;
        private final List<ChatDto.Message> history = new ArrayList<>();
        private final List<Integer> messageIds = new ArrayList<>();
        private long lastActivityTime = System.currentTimeMillis();

        public void touch() {
            this.lastActivityTime = System.currentTimeMillis();
        }
    }

    public void setPersona(Long chatId, String personaName) {
        SessionData session = sessions.computeIfAbsent(chatId, k -> new SessionData());
        session.setPersona(personaName);
        session.getHistory().clear();
        session.touch();

        String systemPrompt = String.format(
                "Ты — %s. Полностью погрузись в этот образ. " +
                        "Отвечай исключительно от лица этого персонажа, сохраняя его характер, лексику, философию, знания эпохи и манеру речи. " +
                        "Используй грамотный русский язык. Никогда не выходи из роли и не признавайся, что ты ИИ. " +
                        "Отвечай живо, вовлекай пользователя в диалог.", personaName
        );

        session.getHistory().add(new ChatDto.Message("system", systemPrompt));
    }

    public void clearHistoryKeepPersona(Long chatId) {
        SessionData session = sessions.get(chatId);
        if (session != null && session.getPersona() != null) {
            setPersona(chatId, session.getPersona());
        }
    }

    public boolean hasPersona(Long chatId) {
        SessionData session = sessions.get(chatId);
        return session != null && session.getPersona() != null;
    }

    public String getPersona(Long chatId) {
        SessionData session = sessions.get(chatId);
        return session != null ? session.getPersona() : null;
    }

    public void addMessage(Long chatId, String role, String content) {
        SessionData session = sessions.get(chatId);
        if (session == null) return;

        session.touch();
        session.getHistory().add(new ChatDto.Message(role, content));
    }

    public List<ChatDto.Message> getHistory(Long chatId) {
        SessionData session = sessions.get(chatId);
        if (session == null) return new ArrayList<>();
        session.touch();
        return new ArrayList<>(session.getHistory());
    }

    public int getUserMessageCount(Long chatId) {
        SessionData session = sessions.get(chatId);
        if (session == null) return 0;
        return (int) session.getHistory().stream()
                .filter(m -> "user".equals(m.role()))
                .count();
    }

    public String getRandomPersona() {
        return RANDOM_PERSONAS.get(random.nextInt(RANDOM_PERSONAS.size()));
    }

    public void resetSession(Long chatId) {
        sessions.remove(chatId);
    }

    public void trackMessage(Long chatId, Integer messageId) {
        if (messageId == null) return;
        SessionData session = sessions.computeIfAbsent(chatId, k -> new SessionData());
        session.getMessageIds().add(messageId);
        if (session.getMessageIds().size() > 50) {
            session.getMessageIds().remove(0);
        }
    }

    public List<Integer> getTrackedMessageIds(Long chatId) {
        SessionData session = sessions.get(chatId);
        if (session == null) return new ArrayList<>();
        return new ArrayList<>(session.getMessageIds());
    }

    public void clearTrackedMessages(Long chatId) {
        SessionData session = sessions.get(chatId);
        if (session != null) {
            session.getMessageIds().clear();
        }
    }

    public List<Long> getInactiveChatIds(long inactivityMs) {
        long now = System.currentTimeMillis();
        List<Long> inactiveChats = new ArrayList<>();
        sessions.forEach((chatId, session) -> {
            if ((now - session.getLastActivityTime()) > inactivityMs) {
                if (!session.getMessageIds().isEmpty() || session.getHistory().size() > 1) {
                    inactiveChats.add(chatId);
                }
            }
        });
        return inactiveChats;
    }
}