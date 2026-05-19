package com.sportlink.notificationservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class InteractionServiceClient {

    private final RestTemplate restTemplate;
    private final String interactionBaseUrl;

    public InteractionServiceClient(
            RestTemplate restTemplate,
            @Value("${sportlink.services.interaction.url:http://localhost:8084}") String interactionBaseUrl) {
        this.restTemplate = restTemplate;
        this.interactionBaseUrl = interactionBaseUrl;
    }

    public List<UUID> getFollowerIds(UUID userId) {
        try {
            String url = interactionBaseUrl + "/api/users/" + userId + "/followers?size=1000";
            var response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getBody() == null) return Collections.emptyList();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            if (content == null) return Collections.emptyList();
            return content.stream()
                    .map(u -> UUID.fromString(u.get("userId").toString()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch followers for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
