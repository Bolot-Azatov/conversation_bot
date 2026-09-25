package org.conversation.service;

import org.conversation.dto.ChatDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AiService(
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.model}") String model) {
        this.restClient = RestClient.builder().baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateResponse(List<ChatDto.Message> conversationHistory) {
        ChatDto.Request request = new ChatDto.Request(model, conversationHistory, 0.6);

        try {
            ChatDto.Response response = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatDto.Response.class);

            if (response != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Извините, мои мысли спутались... (Ошибка обращения к ИИ)";
        }
        return "Не удалось получить ответ.";
    }
}