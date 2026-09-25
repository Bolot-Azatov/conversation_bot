package org.conversation.dto;

import java.util.List;

public class ChatDto {
    public record Request(String model, List<Message> messages, double temperature) {}
    public record Message(String role, String content) {}
    public record Response(List<Choice> choices) {}
    public record Choice(Message message) {}
}