package org.conversation.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static InlineKeyboardMarkup createPopularPersonasKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                createInlineButton("🏛 Сократ", "persona:Сократ"),
                createInlineButton("⚔️ Македонский", "persona:Александр Македонский")
        ));

        rows.add(List.of(
                createInlineButton("🔬 Эйнштейн", "persona:Альберт Эйнштейн"),
                createInlineButton("⚡ Никола Тесла", "persona:Никола Тесла")
        ));

        rows.add(List.of(
                createInlineButton("🕵️‍♂️ Шерлок Холмс", "persona:Шерлок Холмс"),
                createInlineButton("🎲 Случайный", "persona:RANDOM")
        ));

        markup.setKeyboard(rows);
        return markup;
    }

    // Меню: ровно по 2 кнопки в ряд
    public static ReplyKeyboardMarkup createMainChatKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<>();

        // Ряд 1: Смена собеседника и сброс контекста ИИ
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🔄 Сменить собеседника");
        row1.add("🧠 Сбросить память");

        // Ряд 2: Физическое удаление сообщений и статус
        KeyboardRow row2 = new KeyboardRow();
        row2.add("🗑 Удалить сообщения");
        row2.add("👤 Кто собеседник?");

        // Ряд 3: Помощь в диалоге и информация
        KeyboardRow row3 = new KeyboardRow();
        row3.add("💡 О чем спросить?");
        row3.add("📜 Досье");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        markup.setKeyboard(rows);
        return markup;
    }

    private static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}